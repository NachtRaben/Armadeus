package com.nachtraben.core.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import net.dv8tion.jda.core.entities.TextChannel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyGuildConfig implements CustomJsonIO {

    // Configuration objects
    public String adminLogChannelId;
    public String genericLogChannelId;
    public String musicLogChannelId;

    // Custom prefixes
    public List<String> guildPrefixes;

    public boolean deleteCommandMessages = false;

    public Map<String, Object> metadata;

    // Config
    public String id;
    public TextChannel adminLogChannel;
    public TextChannel genericLogChannel;
    public TextChannel musicLogChannel;

    @Override
    public JsonElement write() {
        JsonObject jo = new JsonObject();
        jo.addProperty("adminLogChannelId", adminLogChannelId);
        jo.addProperty("genericLogChannelId", genericLogChannelId);
        jo.addProperty("musicLogChannelId", musicLogChannelId);
        jo.addProperty("deleteCommandMessages", deleteCommandMessages);
        jo.add("guildPrefixes", ConfigurationUtils.GSON_P.toJsonTree(guildPrefixes));
        jo.add("metadata", ConfigurationUtils.GSON_P.toJsonTree(metadata));
        return jo;
    }

    @Override
    public void read(JsonElement me) {
        if (me instanceof JsonObject) {
            JsonObject jo = me.getAsJsonObject();
            if (jo.has("adminLogChannelId"))
                adminLogChannelId = jo.get("adminLogChannelId").getAsString();
            if (jo.has("genericLogChannelId"))
                genericLogChannelId = jo.get("genericLogChannelId").getAsString();
            if (jo.has("musicLogChannelId"))
                musicLogChannelId = jo.get("musicLogChannelId").getAsString();
            if (jo.has("deleteCommandMessages"))
                deleteCommandMessages = jo.get("deleteCommandMessages").getAsBoolean();
            Type type = new TypeToken<List<String>>() {
            }.getType();
            if (jo.has("guildPrefixes"))
                guildPrefixes = ConfigurationUtils.GSON_P.fromJson(jo.get("guildPrefixes"), type);
            type = new TypeToken<Map<String, Object>>() {
            }.getType();
            if (jo.has("metadata"))
                metadata = ConfigurationUtils.GSON_P.fromJson(jo.get("metadata"), type);
        }
    }
}

