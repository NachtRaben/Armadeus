package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.DiscordBot;
import com.nachtraben.pineappleslice.redis.Redis;
import com.nachtraben.pineappleslice.redis.RedisProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class RedisConfig extends BotConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create(); 

    private RedisProvider redis;
    private Redis connection;

    public RedisConfig(RedisProvider redis) {
        this.redis = redis;
        this.connection = redis.getSession(15);
    }

    @Override
    public String getBotToken() {
        return connection.hget("config", "botToken");
    }

    @Override
    public int getShardCount() {
        return Integer.parseInt(connection.hget("config", "shardCount"));
    }

    @Override
    public List<Long> getOwnerIDs() {
        String json = connection.hget("config", "ownerIDs");
        return Arrays.asList(gson.fromJson(json, Long[].class));
    }

    @Override
    public List<Long> getDeveloperIDs() {
        String json = connection.hget("config", "developerIDs");
        return Arrays.asList(gson.fromJson(json, Long[].class));
    }

    @Override
    public List<String> getDefaultPrefixes() {
        String json = connection.hget("config", "defaultPrefixes");
        return Arrays.asList(gson.fromJson(json, String[].class));
    }

    @Override
    public Long getErrorLogChannelId() {
        return Long.parseLong(connection.hget("config", "errorLogChannelId"));
    }

    @Override
    public void read(JsonElement me) {
        super.read(me);
        List<String> arguments = Arrays.asList(DiscordBot.PROGRAM_ARGS);
        if(arguments.contains("--reconfigure")) {
            reconfigure();
        }
    }

    private void reconfigure() {
        LOGGER.warn("Reconfiguration requested!");
        Scanner scanner = new Scanner(System.in);
        Boolean yn;
        String response;
        do {
            LOGGER.info("Do you wish to export the current config to redis? [Y/N]:");
            response = scanner.nextLine();
        } while(!response.matches("[YyNn]"));
        yn = response.matches("[Yy]");
        if(yn) {
            LOGGER.info("Exporting config to redis...");
            connection.hset("config", "botToken", super.getBotToken());
            connection.hset("config", "shardCount", String.valueOf(super.getShardCount()));
            connection.hset("config", "ownerIDs", gson.toJson(super.getOwnerIDs(), TypeToken.getParameterized(ArrayList.class, Long.class).getType()));
            connection.hset("config", "developerIDs", gson.toJson(super.getDeveloperIDs(), TypeToken.getParameterized(ArrayList.class, Long.class).getType()));
            connection.hset("config", "defaultPrefixes", gson.toJson(super.getDefaultPrefixes(), TypeToken.getParameterized(ArrayList.class, Long.class).getType()));
            connection.hset("config", "errorLogChannelId", String.valueOf(super.getErrorLogChannelId()));
            LOGGER.info("Export finished!");
        } else {
            LOGGER.info("Skipping reconfiguration.");
        }
    }

}
