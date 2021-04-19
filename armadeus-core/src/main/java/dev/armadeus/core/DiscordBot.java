package dev.armadeus.core;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.JDACommandExecutionContext;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.JDAOptions;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.configuration.GuildConfig;
import dev.armadeus.core.managers.ExecutorServiceEventManager;
import dev.armadeus.core.managers.GuildManager;
import dev.armadeus.core.util.DiscordMetrics;
import dev.armadeus.core.util.DiscordReference;
import dev.armadeus.core.util.Utils;
import groovy.lang.Tuple3;
import joptsimple.OptionSet;
import lavalink.client.io.LavalinkLoadBalancer;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;

@Getter
public abstract class DiscordBot {

    private static DiscordBot instance;
    @Getter
    private static OptionSet options;
    private final Logger logger = LogManager.getLogger();
    private final boolean logMessages = false;
    private ShardManager shardManager;
    private GuildManager guildManager;
    private Thread shutdownHandler;
    private DiscordMetrics dmetrics;
    private JdaLavalink lavalink;
    private JDACommandManager commandManager;
    private boolean running = false;

    public DiscordBot(OptionSet options) {
        checkState(instance == null, "The DiscordBot has already been initialized!");
        instance = this;
        DiscordBot.options = options;
    }

    public static DiscordBot get() {
        return instance;
    }

    public void start() {
        shutdownHandler = new Thread(this::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHandler);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception in { " + t.getName() + " }.");
            e.printStackTrace();
        });
        running = true;
        logger.info("Loading configuration from file.");
        BotConfig.load();
        BotConfig.save();

        guildManager = new GuildManager(this);
        logger.info("Configuration loaded.");

        logger.info("Loading lavalink...");
        lavalink = new JdaLavalink(1, shardId -> shardManager.getShardById(shardId));
        lavalink.getLoadBalancer().addPenalty(LavalinkLoadBalancer.Penalties::getTotal);

//        commandBase = new CommandBase();

        logger.info("Loading JDA...");
        try {
            shardManager = DefaultShardManagerBuilder.createDefault(BotConfig.get().getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .enableCache(EnumSet.allOf(CacheFlag.class))
                    .setEventManagerProvider(ExecutorServiceEventManager::get)
                    .setActivity(Activity.playing("Armadeus"))
                    .setMemberCachePolicy(MemberCachePolicy.ALL) // Fetch ALL members
                    .setChunkingFilter(ChunkingFilter.ALL) // Chunk member data
                    .addEventListeners(lavalink) // Lavalink Listener
                    .setEnableShutdownHook(false)
                    .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor()) // Lavalink websocket listeners
                    .setShardsTotal(BotConfig.get().getShardCount())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to start Armadeus", e);
            System.exit(-1);
        }
        while (shardManager.getShards().stream().anyMatch(jda -> jda.getStatus() != JDA.Status.CONNECTED)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }

        // Lavalink initialization
        lavalink.setUserId(shardManager.getShards().get(0).getSelfUser().getId());
        for (Tuple3<String, String, String> node : BotConfig.get().getLavalinkNodes()) {
            try {
                lavalink.addNode(node.getV1(), new URI(node.getV2()), node.getV3());
            } catch (URISyntaxException e) {
                logger.error("URI error while mapping lavalink nodes", e);
            }
        }

        // Aikar Transition
        JDAOptions options = new JDAOptions();
        options.configProvider(event -> () -> {
            if (isDevMode() && !BotConfig.get().getDeveloperIds().contains(event.getAuthor().getIdLong())) {
                return List.of("\u0000");
            }
            long id = shardManager.getShards().get(0).getSelfUser().getIdLong();
            Set<String> prefixes = new HashSet<>(List.of("<@" + id + "> ", "<@!" + id + "> "));
            if (event.isFromGuild()) {
                GuildConfig config = getGuildManager().getConfigurationFor(event.getGuild());
                prefixes.addAll(config.getPrefixes());
            }
            if (prefixes.size() < 3) {
                prefixes.addAll(BotConfig.get().getGlobalPrefixes());
            }
            return new ArrayList<>(prefixes);
        });

        commandManager = new JDACommandManager(shardManager, options);
        commandManager.getCommandContexts().registerIssuerOnlyContext(DiscordUser.class, c -> new DiscordUser(((JDACommandExecutionContext) c).getIssuer().getEvent()));
        commandManager.getCommandConditions().addCondition("developeronly", context -> {
            if (!BotConfig.get().getDeveloperIds().contains(context.getIssuer().getEvent().getAuthor().getIdLong())) {
                throw new ConditionFailedException("Only the bot developers can use this command");
            }
        });
    }

    public void shutdown() {
        shutdown(0);
    }

    public boolean isDevMode() {
        return options.has("dev-mode");
    }

    public void shutdown(int code) {
        if (shutdownHandler != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHandler);
            } catch (Exception ignored) {
            }
        }
        running = false;
        DiscordMetrics.shutdown();
        Utils.stopExecutors();
        Iterator<Map.Entry<DiscordReference<Message>, CompletableFuture<?>>> it = DiscordUser.getPendingDeletions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DiscordReference<Message>, CompletableFuture<?>> entry = it.next();
            Message message = entry.getKey().resolve();
            if (message != null) {
                message.delete().complete();
            }
            it.remove();
        }
        lavalink.getLinks().forEach(Link::destroy);
        lavalink.shutdown();
        shardManager.shutdown();
        Runtime.getRuntime().halt(code);
    }

    protected void postStart() {
        dmetrics = new DiscordMetrics(this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
