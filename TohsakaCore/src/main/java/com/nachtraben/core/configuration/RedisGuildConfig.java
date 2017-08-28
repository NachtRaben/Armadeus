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
        if(getConfigFile().exists())
            super.load();

        if(logChannels == null)
            logChannels = new HashMap<>();
        if(channelCache == null)
            channelCache = new HashMap<>();

        Map<String, String> config = getGuildManager().getConnection().hgetall(String.valueOf(getGuildID()));
        if(config.containsKey("deleteCommands"))
            deleteCommands = Boolean.parseBoolean(config.get("deleteCommands"));
        if(config.containsKey("prefixes"))
            prefixes = GSON.fromJson(config.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        if(config.containsKey("disabledCommands"))
            disabledCommands = GSON.fromJson(config.get("disabledCommands"), TypeToken.getParameterized(HashSet.class, String.class).getType());
        if(config.containsKey("blacklistedIDs"))
            blacklistedIDs = GSON.fromJson(config.get("blacklistedIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
        if(config.containsKey("logChannels"))
            logChannels = GSON.fromJson(config.get("logChannels"), TypeToken.getParameterized(HashMap.class, String.class, Long.class).getType());
        if(config.containsKey("genericLogChannelID")) {
            long id = Long.parseLong(config.get("genericLogChannelID"));
            if(id > 0)
                logChannels.put(ChannelTarget.GENERIC.toString().toLowerCase(), id);

            if(guildManager.getConnection().hdel(String.valueOf(guildID), "genericLogChannelID") != 1)
                LOGGER.warn("Failed to delete genericLogChannelID");
            save();
        }
        if(config.containsKey("musicLogChannelID")) {
            long id = Long.parseLong(config.get("musicLogChannelID"));
            if(id > 0)
                logChannels.put(ChannelTarget.MUSIC.toString().toLowerCase(), id);

            if(guildManager.getConnection().hdel(String.valueOf(guildID), "musicLogChannelID") != 1)
                LOGGER.warn("Failed to delete musicLogChannelID");
            save();
        }
        return this;
    }

    public RedisGuildConfig save() {
        getGuildManager().getConnection().hmset(String.valueOf(getGuildID()), toMap());
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
        return result;
    }
}
