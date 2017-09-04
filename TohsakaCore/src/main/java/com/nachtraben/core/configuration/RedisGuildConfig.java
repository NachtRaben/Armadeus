package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.util.ChannelTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RedisGuildConfig extends GuildConfig implements RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisGuildConfig.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public RedisGuildConfig(GuildManager manager, Long guild) {
        super(manager, guild);
    }

    public RedisGuildConfig load() {
        if (getConfigFile().exists())
            super.load();

        super.preInit();
        Map<String, String> config = getGuildManager().runQuery(redis -> redis.hgetall(String.valueOf(getGuildID())));
        if (config.containsKey("deleteCommands"))
            deleteCommands = Boolean.parseBoolean(config.get("deleteCommands"));
        if (config.containsKey("prefixes"))
            prefixes = GSON.fromJson(config.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        if (config.containsKey("disabledCommands"))
            disabledCommands = GSON.fromJson(config.get("disabledCommands"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        if (config.containsKey("blacklistedIDs"))
            blacklistedIDs = GSON.fromJson(config.get("blacklistedIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        if (config.containsKey("logChannels"))
            logChannels = GSON.fromJson(config.get("logChannels"), TypeToken.getParameterized(HashMap.class, String.class, Long.class).getType());
        if (config.containsKey("metadata"))
            metadata = GSON.fromJson(config.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());

        if (config.containsKey("genericLogChannelID")) {
            long id = Long.parseLong(config.get("genericLogChannelID"));
            if (id > 0)
                logChannels.put(ChannelTarget.GENERIC.toString().toLowerCase(), id);
            getGuildManager().runQuery(redis -> redis.hdel(String.valueOf(getGuildID()), "genericLogChannelID"));
            save();
        }
        if (config.containsKey("musicLogChannelID")) {
            long id = Long.parseLong(config.get("musicLogChannelID"));
            if (id > 0)
                logChannels.put(ChannelTarget.MUSIC.toString().toLowerCase(), id);
            getGuildManager().runQuery(redis -> redis.hdel(String.valueOf(getGuildID()), "musicLogChannelID"));
            save();
        }
        super.postInit();
        return this;
    }

    public RedisGuildConfig save() {
        getGuildManager().runQuery(redis -> {
            redis.hmset(String.valueOf(getGuildID()), toMap());
            return null;
        });
        return this;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();
        result.put("deleteCommands", String.valueOf(shouldDeleteCommands()));
        result.put("prefixes", GSON.toJson(getPrefixes()));
        result.put("disabledCommands", GSON.toJson(getDisabledCommands()));
        result.put("blacklistedIDs", GSON.toJson(getBlacklistedIDs()));
        result.put("logChannels", GSON.toJson(getLogChannels()));
        result.put("metadata", GSON.toJson(getMetadata()));
        return result;
    }

}
