package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.util.RedisUtil;
import com.nachtraben.core.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RedisBotConfig extends BotConfig implements RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisBotConfig.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private ScheduledFuture<?> debugFuture;

    public RedisBotConfig(DiscordBot bot) {
        super(bot);
    }

    @Override
    public BotConfig load() {
        super.load();
        List<String> arguments = Arrays.asList(DiscordBot.PROGRAM_ARGS);
        if (arguments.contains("--reconfigure")) {
            reconfigure();
        }
        Map<String, String> config = RedisUtil.runLegacyQuery(15, redis -> redis.hgetall("config"));
        botToken = config.get("botToken");
        shardCount = Integer.parseInt(config.get("shardCount"));
        prefixes = GSON.fromJson(config.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        ownerIDs = GSON.fromJson(config.get("ownerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        developerIDs = GSON.fromJson(config.get("developerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        if (config.containsKey("metadata"))
            metadata = GSON.fromJson(config.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, Object.class).getType());
        errorLogChannelID = Long.valueOf(config.get("errorLogChannelID"));
        return this;
    }

    @Override
    public BotConfig save() {
        RedisUtil.runLegacyQuery(15, redis -> {
            redis.hmset("config", toMap());
            return null;
        });
        return this;
    }

    private void reconfigure() {
        log.warn("Reconfiguration requested!");
        Scanner scanner = new Scanner(System.in);
        Boolean yn;
        String response;
        do {
            log.info("Do you wish to export the current config to provider? [Y/N]:");
            response = scanner.nextLine();
        } while (!response.matches("[YyNn]"));
        yn = response.matches("[Yy]");
        if (yn) {
            log.info("Exporting config to provider...");
            RedisUtil.runLegacyQuery(15, redis -> redis.del("config"));
            save();
            log.info("Export finished!");
        } else {
            log.info("Skipping reconfiguration.");
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
        config.put("metadata", GSON.toJson(getMetadata()));
        config.put("blacklistedIDs", GSON.toJson(getBlacklistedIDs()));
        config.put("errorLogChannelID", String.valueOf(getErrorLogChannelID()));
        return config;
    }

    public boolean isDebugging() {
        try {
            return RedisUtil.runLegacyQuery(15, redis -> redis.get("debug") != null);
        } catch (Exception ignored){}
        return false;
    }

    public void setDebugging(boolean debugging) {
        try {
            if(debugging && debugFuture == null) {
                log.debug("Starting debug update task for redis.");
                debugFuture = Utils.getScheduler().scheduleAtFixedRate(() -> {
                    RedisUtil.runLegacyQuery(15, redis -> {
                        redis.getJedis().setnx("debug", String.valueOf(true));
                        redis.expire("debug", 10);
                        return null;
                    });
                }, 0L, 5L, TimeUnit.SECONDS);
            } else if(!debugging && debugFuture != null) {
                log.debug("Stopping debug update task for redis.");
                debugFuture.cancel(false);
                debugFuture = null;
            }
        } catch (Exception e) {
            log.warn("Failed to set debugging state.", e);
        }
    }

}
