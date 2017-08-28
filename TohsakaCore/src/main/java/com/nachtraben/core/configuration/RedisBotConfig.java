package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.DiscordBot;
import com.nachtraben.pineappleslice.redis.Redis;
import com.nachtraben.pineappleslice.redis.RedisProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisBotConfig extends BotConfig implements RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBotConfig.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private RedisProvider provider;
    private Redis connection;

    public RedisBotConfig(DiscordBot bot, RedisProvider provider) {
        super(bot);
        this.provider = provider;
    }


    @Override
    public BotConfig load() {
        super.load();
        List<String> arguments = Arrays.asList(DiscordBot.PROGRAM_ARGS);
        if (arguments.contains("--reconfigure")) {
            reconfigure();
        }
        Map<String, String> config = getConnection().hgetall("config");
        botToken = config.get("botToken");
        shardCount = Integer.parseInt(config.get("shardCount"));
        prefixes = GSON.fromJson(config.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        ownerIDs = GSON.fromJson(config.get("ownerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        developerIDs = GSON.fromJson(config.get("developerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        errorLogChannelID = Long.valueOf(config.get("errorLogChannelID"));
        return this;
    }

    @Override
    public BotConfig save() {
        getConnection().hmset("config", toMap());
        return this;
    }

    public Redis getConnection() {
        if(connection == null) {
            return connection = provider.getSession(15);
        }
        return connection;
    }

    private void reconfigure() {
        LOGGER.warn("Reconfiguration requested!");
        Scanner scanner = new Scanner(System.in);
        Boolean yn;
        String response;
        do {
            LOGGER.info("Do you wish to export the current config to provider? [Y/N]:");
            response = scanner.nextLine();
        } while (!response.matches("[YyNn]"));
        yn = response.matches("[Yy]");
        if (yn) {
            LOGGER.info("Exporting config to provider...");
            getConnection().del("config");
            getConnection().hmset("config", toMap());
            LOGGER.info("Export finished!");
        } else {
            LOGGER.info("Skipping reconfiguration.");
        }
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> config = new HashMap<>();
        config.put("botToken", getBotToken());
        config.put("shardCount", String.valueOf(getShardCount()));
        config.put("prefixes", GSON.toJson(getPrefixes()));
        config.put("ownerIDs", GSON.toJson(getOwnerIDs()));
        config.put("developerIDs", GSON.toJson(getDeveloperIDs()));
        config.put("blacklistedIDs", GSON.toJson(getBlacklistedIDs()));
        config.put("errorLogChannelID", String.valueOf(getErrorLogChannelID()));
        return config;
    }

    public boolean isDebugging() {
        try {
            return Boolean.parseBoolean(connection.get("debug"));
        } catch (Exception ignored){};
        return false;
    }

    public void setDebugging(boolean debugging) {
        try {
            connection.set("debug", String.valueOf(debugging));
            connection.expire("debug", Math.toIntExact(TimeUnit.MINUTES.toSeconds(10)));
        } catch (Exception e) {
            LOGGER.warn("Failed to set debugging state.", e);
        }
    }

}
