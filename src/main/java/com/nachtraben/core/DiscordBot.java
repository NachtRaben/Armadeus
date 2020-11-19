package com.nachtraben.core;

import com.nachtraben.core.audio.AudioPlayerSendHandler;
import com.nachtraben.core.command.ConsoleCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.listeners.DiscordCommandListener;
import com.nachtraben.core.listeners.WelcomeListener;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.util.DiscordMetrics;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandBase;
import lavalink.client.io.LavalinkLoadBalancer;
import lavalink.client.io.jda.JdaLavalink;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public abstract class DiscordBot {

    public static String[] PROGRAM_ARGS;

    private final Logger logger;

    private ShardManager shardManager;
    private final GuildManager guildManager;
    private final CommandBase commandBase;
    private final BotConfig config;
    private DiscordCommandListener commandListener;
    private final Thread shutdownHandler;
    private DiscordMetrics dmetrics;
    private final JdaLavalink lavalink;

    private boolean running = false;
    private boolean debugging = false;
    private boolean logMessages = false;

    @SneakyThrows
    public DiscordBot(String[] args) {
        ConsoleCommandSender.start();
        PROGRAM_ARGS = args;
        shutdownHandler = new Thread(this::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHandler);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception in { " + t.getName() + " }.");
            e.printStackTrace();
        });
        running = true;
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Loading configuration from file.");
        config = new BotConfig(this).load();

        guildManager = new GuildManager(this);
        logger.info("Configuration loaded.");

        logger.info("Loading lavalink...");
        lavalink = new JdaLavalink(isDebugging() ? "270410992536649738" : "270410992536649738", 1, shardId -> shardManager.getShardById(shardId));
        lavalink.getLoadBalancer().addPenalty(LavalinkLoadBalancer.Penalties::getPlayerPenalty);
        lavalink.addNode("Armadeus", new URI("ws://ns534168.ip-149-56-240.net:2333"), "fluffy");
        lavalink.addNode("Armadeus2", new URI("ws://ns534168.ip-149-56-240.net:2334"), "fluffy");

        commandBase = new CommandBase();
        logger.info("Loading JDA...");
        try {
            shardManager = DefaultShardManagerBuilder.createDefault(config.getBotToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .setActivity(Activity.playing("Armadeus"))
                    .setMemberCachePolicy(MemberCachePolicy.ALL) // Fetch ALL members
                    .setChunkingFilter(ChunkingFilter.ALL) // Chunk member data
                    .addEventListeners(lavalink) // Lavalink Listener
                    .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor()) // Lavalink websocket listeners
                    .setShardsTotal(1) // Defaults to 1 as we aren't serving a large number of guilds
                    .addEventListeners(new DiscordCommandListener(this), new WelcomeListener(this))
                    .build();

        } catch (Exception e) {
            logger.error("Failed to start Armadeus", e);
            System.exit(-1);
        }
        while (shardManager.getShards().stream().anyMatch(jda -> jda.getStatus() != JDA.Status.CONNECTED)) {
            Thread.sleep(100);
        }
    }

    public JdaLavalink getLavalink() {
        return lavalink;
    }

    protected void postStart() {
        dmetrics = new DiscordMetrics(this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public CommandBase getCommandBase() {
        return commandBase;
    }

    public BotConfig getConfig() {
        return config;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        logger.info("Debugging is now set to { " + debugging + " }.");
        this.debugging = debugging;
    }

    public void logMessages(boolean log) {
        this.logMessages = log;
    }

    public boolean isLogMessages() {
        return this.logMessages;
    }

    public void setAudioDebug(boolean b) {
        AudioPlayerSendHandler.DEBUG = b;
    }

    public void shutdown() {
        shutdown(0);
    }

    public void shutdown(int code) {
        if (shutdownHandler != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHandler);
            } catch (Exception ignored) {
            }
        }

        running = false;
        ConsoleCommandSender.stop();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
        DiscordMetrics.shutdown();
        shardManager.shutdown();
        Utils.stopExecutors();
        Runtime.getRuntime().halt(code);
    }
}
