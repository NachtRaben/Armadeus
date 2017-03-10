package com.nachtraben.core.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nachtraben.core.JDABot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by NachtRaben on 2/17/2017.
 */
public class BotConfig implements JsonIO {

    private int shardCount = 1;
    private String token = "";
    private List<String> defaultCommandPrefixes = Arrays.asList(".", ",");
    private String globalLogChannel;

    @Override
    public JsonElement write() {
        JsonObject jo = new JsonObject();
        jo.addProperty("Shards", 1);
        jo.addProperty("ClientToken", token);
        jo.add("DefaultCommandPrefixes", JsonLoader.GSON_P.toJsonTree(defaultCommandPrefixes));
        jo.addProperty("GlobalLogChannel", globalLogChannel);
        return jo;
    }

    @Override
    public void read(JsonElement me) {
        if(me instanceof JsonObject) {
            JsonObject jo = me.getAsJsonObject();
            shardCount = jo.get("Shards").getAsInt();
            token = jo.get("ClientToken").getAsString();
            defaultCommandPrefixes = new ArrayList<>();
            jo.getAsJsonArray("DefaultCommandPrefixes").forEach(jsonElement -> defaultCommandPrefixes.add(jsonElement.getAsString()));
            if(jo.has("GlobalLogChannel")) globalLogChannel = jo.get("GlobalLogChannel").getAsString();
        }
    }

    @Override
    public void onCreate() {
        System.out.println("Please change your bot token.");
        JDABot.getInstance().shutdown();
    }

    public BotConfig load() {
        JsonLoader.loadFile(JsonLoader.BASE_DIR, "config.json", this);
        return this;
    }

    public BotConfig save() {
        JsonLoader.saveFile(JsonLoader.BASE_DIR, "config.json", this);
        return this;
    }

    public int getShardCount() {
        return shardCount;
    }

    public String getToken() {
        return token;
    }

    public List<String> getDefaultCommandPrefixes() {
        return defaultCommandPrefixes;
    }

    public String getGlobalLogChannel() {
        return globalLogChannel;
    }

    public void setGlobalLogChannel(String globalLogChannel) {
        this.globalLogChannel = globalLogChannel;
    }
}
