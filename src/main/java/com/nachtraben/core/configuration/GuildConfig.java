package com.nachtraben.core.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NachtRaben on 2/20/2017.
 */
public class GuildConfig implements JsonIO {

    // Configuration objects
    private Map<String, Object> metadata;

    // Config
    private String id;

    public GuildConfig(String id) {
        this.id = id;
        metadata = new HashMap<>();
    }

    @Override
    public JsonElement write() {
        JsonObject jo = new JsonObject();
        jo.add("metadata", JsonLoader.GSON_P.toJsonTree(metadata));
        return jo;
    }

    @Override
    public void read(JsonElement me) {
        if(me instanceof JsonObject) {
            JsonObject jo = me.getAsJsonObject();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            metadata = JsonLoader.GSON_P.fromJson(jo.get("metadata"), type);
        }
    }

    @Override
    public void onCreate() {
        System.out.println("Created new guild configuration for: " + id);
    }

    public GuildConfig load() {
        if(!JsonLoader.GUILD_DIR.exists()) JsonLoader.GUILD_DIR.mkdirs();
        JsonLoader.loadFile(JsonLoader.GUILD_DIR, id + ".json", this);
        return this;
    }

    public GuildConfig save() {
        JsonLoader.saveFile(JsonLoader.GUILD_DIR, id + ".json", this);
        return this;
    }

}
