package com.nachtraben.core;

import com.nachtraben.core.audio.AudioPlayerSendHandler;
import com.nachtraben.core.command.ConsoleCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.configuration.RedisBotConfig;
import com.nachtraben.core.listeners.DiscordCommandListener;
import com.nachtraben.core.listeners.FileUploadListener;
import com.nachtraben.core.listeners.LogbackListener;
import com.nachtraben.core.listeners.WelcomeListener;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.managers.ShardManager;
import com.nachtraben.core.util.DiscordMetrics;
import com.nachtraben.core.util.RedisUtil;
import com.nachtraben.core.util.Utils;
import com.nachtraben.core.util.WebhookLogger;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.orangeslice.CommandBase;
import com.nachtraben.pineappleslice.redis.RedisModule;
import com.nachtraben.pineappleslice.redis.RedisProperties;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.nachtraben.core.configuration.GuildConfig.GUILD_DIR;

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

        if (config.isUseRedis()) {
            logger.info("Using redis backend.");
            redisModule = new RedisModule(new RedisProperties(config.getRedisHost(), config.getRedisPort(), config.getRedisPassword(), config.getRedisTimeout()));
            RedisUtil.setModule(redisModule);
            config = new RedisBotConfig(this).load();
            guildManager = new GuildManager(this, redisModule.getProvider());
        } else {
            guildManager = new GuildManager(this);
        }
        logger.info("Configuration loaded.");

        commandBase = new CommandBase();
        commandListener = new DiscordCommandListener(this);

        shardManager = new ShardManager(this);
        shardManager.addDefaultListener(new DiscordCommandListener(this), new WelcomeListener(this));
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
        if (Arrays.asList(PROGRAM_ARGS).contains("--dump") && config instanceof RedisBotConfig) {
            dumpGuildData();
        }
        try {
            Thread.sleep(5000);
            guildManager.loadPersistInformation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dumpGuildData() {
        shardManager.getShards().forEach(shard -> {
            for (Guild guild : shard.getGuilds()) {
                logger.warn("Dumping guild configuration for " + guild);
                GuildConfig config = getGuildManager().getConfigurationFor(guild);
                ConfigurationUtils.saveData(guild.getId() + ".json", GUILD_DIR, config);
            }
        });
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
        ConsoleCommandSender.stop();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
//        guildManager.savePersistInformation();
        DiscordMetrics.shutdown();
        shardManager.shutdownAllShards();
        wlogger.stop();
        Utils.stopExecutors();
        Runtime.getRuntime().halt(code);
    }


    // Metrics
    // TODO: Move these
    public int getTotalChannels() {
        return getShardManager().getShards().stream().mapToInt(shard -> shard.getTextChannels().size() + shard.getVoiceChannels().size() + shard.getPrivateChannels().size()).sum();
    }

    public int getTextChannels() {
        return getShardManager().getShards().stream().mapToInt(shard -> shard.getTextChannels().size()).sum();
    }

    public int getVoiceChannels() {
        return getShardManager().getShards().stream().mapToInt(shard -> shard.getVoiceChannels().size()).sum();

    }

    public int getPrivateChannels() {
        return getShardManager().getShards().stream().mapToInt(shard -> shard.getPrivateChannels().size()).sum();

    }

    public int getGuildCount() {
        return getShardManager().getShards().stream().mapToInt(shard -> shard.getGuilds().size()).sum();
    }

    public int getUserCount() {
        return (int) getShardManager().getShards().stream().flatMap(jda -> jda.getUsers().stream().map(ISnowflake::getIdLong)).distinct().count();
    }

    public int getConnectedVoiceChannels() {
        return (int) getShardManager().getShards().stream().flatMap(shard -> shard.getGuilds().stream().filter(guild -> guild.getAudioManager().isConnected())).count();
    }
}
