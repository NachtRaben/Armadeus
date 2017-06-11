package com.nachtraben.core;

import com.nachtraben.core.command.DiscordCommandBase;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.RedisConfig;
import com.nachtraben.core.listeners.DiscordCommandListener;
import com.nachtraben.core.managers.ShardManager;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.orangeslice.CommandBase;
import com.nachtraben.pineappleslice.redis.RedisModule;
import com.nachtraben.pineappleslice.redis.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class DiscordBot {

    public static String[] PROGRAM_ARGS;

    private Logger logger;

    private RedisModule redisModule;
    private ShardManager shardManager;
    private CommandBase commandBase;
    private BotConfig config;

    private boolean debugging = false;

    public DiscordBot() {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Loading configuration from file.");
        config = new BotConfig();
        config = ConfigurationUtils.load("config.json", new File(System.getProperty("user.dir")), config);

        if(config.isUseRedis()) {
            logger.info("Using redis backend.");
            redisModule = new RedisModule(new RedisProperties(config.getRedisHost(), config.getRedisPort(), config.getRedisPassword(), config.getRedisTimeout()));
            config = new RedisConfig(redisModule.getProvider());
            ConfigurationUtils.load("config.json", new File(System.getProperty("user.dir")), config);
        }
        logger.info("Configuration loaded.");

        commandBase = new DiscordCommandBase(this);
        shardManager = new ShardManager(this);
        shardManager.addDefaultListener(new DiscordCommandListener(this));

    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public CommandBase getCommandBase() {
        return commandBase;
    }

    public BotConfig getConfig() {
        return config;
    }

    public boolean isDebugging() {
        return debugging;
    }
}
