package com.nachtraben.core;

import com.nachtraben.core.audio.AudioPlayerSendHandler;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.RedisBotConfig;
import com.nachtraben.core.listeners.DiscordCommandListener;
import com.nachtraben.core.listeners.FileUploadListener;
import com.nachtraben.core.listeners.LogbackListener;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.managers.ShardManager;
import com.nachtraben.core.util.DiscordMetrics;
import com.nachtraben.core.util.Utils;
import com.nachtraben.core.util.WebhookLogger;
import com.nachtraben.orangeslice.CommandBase;
import com.nachtraben.pineappleslice.redis.RedisModule;
import com.nachtraben.pineappleslice.redis.RedisProperties;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiscordBot {

    public static String[] PROGRAM_ARGS;

    private Logger logger;

    private RedisModule redisModule;
    private ShardManager shardManager;
    private GuildManager guildManager;
    private CommandBase commandBase;
    private BotConfig config;
    private DiscordCommandListener commandListener;
    private Thread shutdownHandler;
    private DiscordMetrics dmetrics;

    private WebhookLogger wlogger;

    private boolean running = false;
    private boolean debugging = false;
    private boolean logMessages = false;

    public DiscordBot(String[] args) {
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

        if (config.isUseRedis()) {
            logger.info("Using redis backend.");
            redisModule = new RedisModule(new RedisProperties(config.getRedisHost(), config.getRedisPort(), config.getRedisPassword(), config.getRedisTimeout()));
            config = new RedisBotConfig(this, redisModule.getProvider()).load();
            guildManager = new GuildManager(this, redisModule.getProvider());
        } else {
            guildManager = new GuildManager(this);
        }
        logger.info("Configuration loaded.");

        commandBase = new CommandBase();
        commandListener = new DiscordCommandListener(this);

        shardManager = new ShardManager(this);
        shardManager.addDefaultListener(new DiscordCommandListener(this), new FileUploadListener());
        LogbackListener.install(this);
    }

    protected void postStart() {
        dmetrics = new DiscordMetrics(this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wlogger = new WebhookLogger(this, 317784247949590528L, 369967452488073216L);
        try {
            wlogger.start();
        } catch (Exception e) {
            logger.error("Failed to initialize fine logger!", e);
            wlogger = null;
        }
        try {
            Thread.sleep(5000);
            guildManager.loadPersistInformation();
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

    public WebhookLogger getWlogger() {
        return wlogger;
    }

    public void setDebugging(boolean debugging) {
        logger.info("Debugging is now set to { " + debugging + " }.");
        this.debugging = debugging;
        if (config instanceof RedisBotConfig)
            ((RedisBotConfig) config).setDebugging(debugging);
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
        if(shutdownHandler != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHandler);
            } catch (Exception ignored) {}
        }

        running = false;
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
        guildManager.savePersistInformation();
        DiscordMetrics.shutdown();
        shardManager.shutdownAllShards();
        wlogger.stop();
        Utils.stopExecutors();
        Runtime.getRuntime().halt(code);
    }

    public int getTotalChannels() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getTextChannels().size();
            i += jda.getVoiceChannels().size();
            i += jda.getPrivateChannels().size();
        }
        return i;
    }

    public int getTextChannels() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getTextChannels().size();
        }
        return i;
    }

    public int getVoiceChannels() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getVoiceChannels().size();
        }
        return i;
    }

    public int getPrivateChannels() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getPrivateChannels().size();
        }
        return i;
    }

    public int getGuildCount() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getGuilds().size();
        }
        return i;
    }

    public int getUserCount() {
        int i = 0;
        for (JDA jda : getShardManager().getShards()) {
            i += jda.getUsers().size();
        }
        return i;
    }

    public int getConnectedVoiceChannels() {
        int i = 0;
        for(JDA jda : getShardManager().getShards()) {
            for(Guild g : jda.getGuilds())
                if(g.getAudioManager().isConnected())
                    i++;
        }
        return i;
    }
}
