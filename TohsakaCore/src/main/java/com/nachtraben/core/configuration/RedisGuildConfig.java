package com.nachtraben.core.configuration;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisGuildConfig extends GuildConfig implements RedisConfig {

    private static final transient Logger log = LoggerFactory.getLogger(RedisGuildConfig.class);

    public RedisGuildConfig(GuildManager manager, Long guild) {
        super(manager, guild);
    }

    public RedisGuildConfig load() {
        if (getConfigFile().exists())
            super.load();

        super.preInit();
        Map<String, String> config = RedisUtil.runQuery(redis -> redis.hgetall(String.valueOf(getGuildID())));
        if (config == null)
            throw new IllegalStateException("Failed to load guild configuration, non-existent or null?");
        if (config.containsKey("deleteCommands"))
            deleteCommands = Boolean.parseBoolean(config.get("deleteCommands"));
        if (config.containsKey("prefixes"))
            prefixes = GSON.fromJson(config.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        if (config.containsKey("disabledCommands"))
            disabledCommands = GSON.fromJson(config.get("disabledCommands"), TypeToken.getParameterized(HashMap.class, String.class, TypeToken.getParameterized(HashSet.class, String.class).getType()).getType());
        if (config.containsKey("isBlacklist"))
            isBlacklist = Boolean.parseBoolean(config.get("isBlacklist"));
        if (config.containsKey("blacklistedIDs"))
            blacklistedIDs = GSON.fromJson(config.get("blacklistedIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        if (config.containsKey("logChannels"))
            logChannels = GSON.fromJson(config.get("logChannels"), TypeToken.getParameterized(HashMap.class, String.class, Long.class).getType());
        if (config.containsKey("metadata"))
            metadata = GSON.fromJson(config.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
        if (config.containsKey("cooldown"))
            cooldown = Long.parseLong(config.get("cooldown"));

        super.postInit();
        return this;
    }

    public RedisGuildConfig save() {
        RedisUtil.runQuery(redis -> {
            redis.hmset(String.valueOf(getGuildID()), toMap());
            return null;
        });
        return this;
    }

    @Override
    public Map<String, String> toMap() {
        JsonObject jo = (JsonObject) GSON.toJsonTree(this);
        Map<String, String> result = jo.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        return result;

//        result.put("deleteCommands", String.valueOf(shouldDeleteCommands()));
//        result.put("prefixes", GSON.toJson(getPrefixes()));
//        result.put("disabledCommands", GSON.toJson(getDisabledCommands()));
//        result.put("isBlacklist", String.valueOf(isBlacklist));
//        result.put("blacklistedIDs", GSON.toJson(getBlacklistedIDs()));
//        result.put("logChannels", GSON.toJson(getLogChannels()));
//        result.put("metadata", GSON.toJson(getMetadata()));
//        result.put("cooldown", GSON.toJson(getCooldown()));
//        return result;
    }

}
