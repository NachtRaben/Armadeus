package dev.armadeus.core;

import co.aikar.commands.ConditionFailedException;
import com.electronwill.nightconfig.core.Config;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.impl.event.VelocityEventManager;
import com.velocitypowered.impl.plugin.VelocityPluginManager;
import com.velocitypowered.impl.scheduler.VelocityScheduler;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.util.DiscordReference;
import dev.armadeus.core.command.CommandSenderImpl;
import dev.armadeus.core.command.JDACommandManager;
import dev.armadeus.core.command.JDAOptions;
import dev.armadeus.core.config.ArmaConfigImpl;
import dev.armadeus.core.guild.GuildManagerImpl;
import dev.armadeus.core.managers.ExecutorServiceEventManager;
import joptsimple.OptionSet;
import lombok.Getter;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;

@Getter
public class ArmaCoreImpl extends ArmaCore {

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
            Path pluginPath = Paths.get("plugins");
            if (!pluginPath.toFile().exists()) {
                Files.createDirectory(pluginPath);
            } else {
                if (!pluginPath.toFile().isDirectory()) {
                    logger.warn("Plugin location {} is not a directory, continuing without loading plugins",
                            pluginPath);
                    return;
                }
                pluginManager.loadPlugins(pluginPath);
            }
        } catch (Exception e) {
            logger.error("Couldn't load plugins", e);
        }

        // Register the plugin main classes so that we can fire the proxy initialize event
        for (PluginContainer plugin : pluginManager.plugins()) {
            Optional<?> instance = plugin.instance();
            if (instance.isPresent()) {
                try {
                    eventManager.registerInternally(plugin, instance.get());
                } catch (Exception e) {
                    logger.error("Unable to register plugin listener for {}",
                            plugin.description().name().orElse(plugin.description().id()), e);
                }
            }
        }
        logger.info("Loaded {} plugins", pluginManager.plugins().size());
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
            if (isDevMode() && !armaConfig.getDeveloperIds().contains(event.getAuthor().getIdLong())) {
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
        commandManager.enableUnstableAPI("help");
        commandManager.getCommandConditions().addCondition("developeronly", context -> {
            if (!armaConfig.getDeveloperIds().contains(context.getIssuer().getEvent().getAuthor().getIdLong())) {
                throw new ConditionFailedException("Only the bot developers can use this command");
            }
        });
        eventManager.fireAndForget(commandManager);
    }

    public boolean isDevMode() {
        return options.has("dev-mode");
    }

//    public void start() {
    // Shutdown Hook
//        logger.info("Installing shutdown hooks...");
//        shutdownHandler = new Thread(this::shutdown);
//        Runtime.getRuntime().addShutdownHook(shutdownHandler);
//        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
//            System.err.println("Uncaught exception in { " + t.getName() + " }.");
//            e.printStackTrace();
//        });
//        logger.info("Shutdown hooks installed");
//
//
//        logger.info("Loading configuration...");
//        ArmaConfigImpl.load();
//        ArmaConfigImpl.save();
//        logger.info("Configuration loaded");
//
//        logger.info("Loading Guild Manager...");
//        guildManager = new GuildManager(this);
//        logger.info("Guild Manager loaded");
//
//        logger.info("Loading LavaLink...");
////
//        logger.info("LavaLink loaded");

    // Lavalink initialization
//        logger.info("Registering LavaLink nodes...");
//        lavalink.setUserId(shardManager.getShards().get(0).getSelfUser().getId());
//        for (Tuple3<String, String, String> node : BotConfig.get().getLavalinkNodes()) {
//            try {
//                lavalink.addNode(node.getV1(), new URI(node.getV2()), node.getV3());
//                logger.info("Registering LavaLinkNode {}@{}", node.getV1(), node.getV2());
//            } catch (URISyntaxException e) {
//                logger.error("URI error while mapping LavaLinkNodes nodes", e);
//            }
//        }
//        logger.info("LavaLink nodes registered");

//        logger.info("Command System Initialized");
//    }

    public void shutdown(boolean explicitExit) throws InterruptedException {
        try {
            String s = String.valueOf(eventManager.fire(String.class).get());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to send shutdown event", e);
        }

        Iterator<Map.Entry<DiscordReference<Message>, CompletableFuture<?>>> it = CommandSenderImpl.getPendingDeletions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DiscordReference<Message>, CompletableFuture<?>> entry = it.next();
            Message message = entry.getKey().resolve();
            if (message != null) {
                message.delete().complete();
            }
            it.remove();
        }
        guildManager.shutdown();
        eventManager.shutdown();
        scheduler.shutdown();
        if(shardManager != null)
            shardManager.shutdown();
    }

//    public void shutdown(int code) {
//        lavalink.getLinks().forEach(link -> {
//            if(link.getChannelId() != -1) {
//                link.getPlayer().stopTrack();
//            }
//            link.destroy();
//        });
}
