package dev.armadeus.core;

import co.aikar.commands.CommandSenderImpl;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.JDAOptions;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.MoreObjects;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.proxy.VelocityManager;
import com.velocitypowered.proxy.event.VelocityEventManager;
import com.velocitypowered.proxy.plugin.VelocityPluginManager;
import com.velocitypowered.proxy.scheduler.VelocityScheduler;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.events.ShutdownEvent;
import dev.armadeus.bot.api.util.DiscordReference;
import dev.armadeus.core.command.CommandCommand;
import dev.armadeus.core.command.ConfCommands;
import dev.armadeus.core.command.HelpCommand;
import dev.armadeus.core.command.PingCommand;
import dev.armadeus.core.command.PrefixesCommand;
import dev.armadeus.core.command.ShutdownCommand;
import dev.armadeus.core.command.SlashCommands;
import dev.armadeus.core.command.UptimeCommand;
import dev.armadeus.core.config.ArmaConfigImpl;
import dev.armadeus.core.managers.ExecutorServiceEventManager;
import dev.armadeus.core.managers.GuildManagerImpl;
import dev.armadeus.core.managers.InstanceManager;
import joptsimple.OptionSet;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;

@Getter
@Accessors(fluent = true)
public class ArmaCoreImpl extends VelocityManager implements ArmaCore {

    private static final Logger logger = LoggerFactory.getLogger(ArmaCore.class);
    private static ArmaCoreImpl instance;
    @Getter
    private static OptionSet options;
    private final boolean logMessages = false;

    // New Variables
    private ArmaConfigImpl armaConfig;
    private VelocityPluginManager pluginManager;
    private VelocityEventManager eventManager;
    private JDACommandManager commandManager;
    private VelocityScheduler scheduler;
    private GuildManagerImpl guildManager;
    private DefaultShardManager shardManager;
    private InstanceManager instanceManager;

    public ArmaCoreImpl(OptionSet options) {
        checkState(instance == null, "The DiscordBot has already been initialized!");
        Config.setInsertionOrderPreserved(true);
        instance = this;
        ArmaCoreImpl.options = options;
        pluginManager = new VelocityPluginManager(this);
        eventManager = new VelocityEventManager(pluginManager);
        commandManager = new JDACommandManager(this);
        scheduler = new VelocityScheduler(pluginManager);
        guildManager = new GuildManagerImpl();
    }

    public static ArmaCoreImpl get() {
        return instance;
    }

    public void start() {
        logger.info("Starting up Arma-Core...");
        loadCoreConfiguration();
        loadPlugins();
        loadGuildManager();
        loadJda();
        loadCommandManager();
        if (armaConfig.isDatabaseEnabled()) {
            instanceManager = new InstanceManager(this);
        }
    }

    private void loadCoreConfiguration() {
        logger.info("Loading configuration...");
        try {
            Path path = Path.of("arma-core.toml");
            armaConfig = (ArmaConfigImpl) ArmaConfigImpl.read(path);
            logger.info("Configuration loaded");
        } catch (Exception e) {
            logger.error("Failed to load/parse/save arma-core.toml. Arma-Core will shutdown", e);
            LogManager.shutdown();
            System.exit(-1);
        }
    }

    private void loadPlugins() {
        logger.info("Loading plugins...");
        try {
            pluginManager.loadPlugins(Path.of("plugins"));

            // Register the plugin main classes so that we can fire the proxy initialize event
            for (PluginContainer plugin : pluginManager.plugins()) {
                Object instance = plugin.instance();
                if (instance != null) {
                    try {
                        eventManager.registerInternally(plugin, instance);
                    } catch (Exception e) {
                        logger.error("Unable to register plugin listener for {}",
                                MoreObjects.firstNonNull(plugin.description().name(), plugin.description().id()), e);
                    }
                }
            }
            logger.info("Loaded {} plugins", pluginManager.plugins().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGuildManager() {
        logger.info("Loading GuildManager...");
        Path guildsPath = Path.of("guilds");
        if (!Files.isDirectory(guildsPath)) {
            try {
                Files.createDirectories(guildsPath);
            } catch (IOException e) {
                logger.error("Failed to create guilds directory " + guildsPath.toAbsolutePath() + " Arma-Core will shutdown", e);
                LogManager.shutdown();
                System.exit(-1);
            }
        }
        guildManager.initialize(this, guildsPath);
        logger.info("GuildManager loaded");
    }

    private void loadJda() {
        logger.info("Loading JDA...");
        AtomicInteger shards = new AtomicInteger(armaConfig.getShardsTotal());
        try {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(armaConfig.getToken());
            builder.setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)); // TODO: Configure by event
            builder.enableCache(EnumSet.allOf(CacheFlag.class)); // TODO: Configure by event
            builder.setEventManagerProvider(ExecutorServiceEventManager::get);
            builder.setEnableShutdownHook(false);
            builder.setShardsTotal(armaConfig.getShardsTotal());
            builder.setActivityProvider(value -> Activity.watching("Arma-Core #" + value));
            builder.setMemberCachePolicy(MemberCachePolicy.ALL); // TODO: Configure by event
            builder.setChunkingFilter(ChunkingFilter.ALL); // TODO: Configure by event
            builder.addEventListeners((EventListener) event -> {
                if (event instanceof ReadyEvent)
                    shards.decrementAndGet();
                eventManager.fireAndForget(event);
            });
            builder = eventManager.fire(builder).get();
            if (!armaConfig.getShards().isEmpty()) {
                builder.setShards(armaConfig.getShards());
            }
            shardManager = (DefaultShardManager) builder.build();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord", e);
            LogManager.shutdown();
            System.exit(-1);
        }

        long lastCheck = 0;
        while (shards.get() != 0) {
            if (lastCheck != (lastCheck = shards.get())) {
                logger.info("Waiting for {} shard(s) to initialize...", lastCheck);
            }
        }
        eventManager.fireAndForget(shardManager);
        logger.info("JDA loaded");
    }

    private void loadCommandManager() {
        logger.info("Initializing command manager...");
        JDAOptions options = new JDAOptions();
        options.configProvider(event -> () -> {
            if (armaConfig.isDevMode() && !armaConfig.getDeveloperIds().contains(event.getAuthor().getIdLong())) {
                return List.of("\u0000");
            }
            long id = shardManager.getShards().get(0).getSelfUser().getIdLong();
            Set<String> prefixes = new HashSet<>(List.of("<@" + id + "> ", "<@!" + id + "> "));
            if (event.isFromGuild()) {
                GuildConfig config = guildManager.getConfigFor(event.getGuild());
                prefixes.addAll(config.getPrefixes());
            }
            if (prefixes.size() < 3) {
                prefixes.addAll(armaConfig.getDefaultPrefixes());
            }
            return new ArrayList<>(prefixes);
        });
        commandManager.initialize(options);
        commandManager.registerDependency(ArmaCore.class, this);
        commandManager.enableUnstableAPI("help");
        commandManager.getCommandConditions().addCondition("developeronly", context -> {
            if (!armaConfig.getDeveloperIds().contains(context.getIssuer().getUser().getIdLong())) {
                throw new ConditionFailedException("Only the bot developers can use this command");
            }
        });
        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new SlashCommands());
        commandManager.registerCommand(new CommandCommand());
        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new PingCommand());
        commandManager.registerCommand(new PrefixesCommand());
        commandManager.registerCommand(new ShutdownCommand());
        commandManager.registerCommand(new UptimeCommand());
        commandManager.registerCommand(new ConfCommands());
        eventManager.fireAndForget(commandManager);
    }

    public void shutdown() {
        shutdown(false);
    }

    @SneakyThrows
    private void shutdown(boolean explicitExit) {
        // Shutdown Event
        eventManager.fire(new ShutdownEvent()).get();
        // Shutdown Dev Instance Manager
        if (instanceManager != null)
            instanceManager.shutdown();
        // Delete pending messages
        Iterator<Map.Entry<DiscordReference<Message>, CompletableFuture<?>>> it = CommandSenderImpl.getPendingDeletions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DiscordReference<Message>, CompletableFuture<?>> entry = it.next();
            Message message = entry.getKey().resolve();
            if (message != null) {
                try {
                    message.delete().complete();
                } catch (Exception ignored) {
                }
            }
            it.remove();
        }
        // Shutdown Guild Manager
        guildManager.shutdown();
        eventManager.shutdown();
        scheduler.shutdown();
        if (shardManager != null)
            shardManager.shutdown();
    }
}
