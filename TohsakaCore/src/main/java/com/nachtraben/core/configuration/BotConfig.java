package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.DiscordBot;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BotConfig implements CustomJsonIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private transient DiscordBot bot;

    String botToken = "";
    int shardCount = -1;
    Set<String> prefixes;

    Set<Long> ownerIDs;
    Set<Long> developerIDs;
    Set<Long> blacklistedIDs;
    Map<String, Object> metadata;

    Long errorLogChannelID = -1L;
    transient TextChannel errorLogChannel;

    private boolean useRedis = false;
    private String redisHost = "localhost";
    private int redisPort = 6379;
    private String redisPassword = "";
    private int redisTimeout = 10000;

    public BotConfig(DiscordBot bot) {
        this.bot = bot;
        this.prefixes = new HashSet<>();
        prefixes.add("-");
        this.ownerIDs = new HashSet<>();
        ownerIDs.add(118255810613608451L);
        this.developerIDs = new HashSet<>();
        developerIDs.add(118255810613608451L);
        this.blacklistedIDs = new HashSet<>();
        this.metadata = new HashMap<>();
        metadata.put("Test", "TestValue");
    }

    @Override
    public JsonElement write() {
        JsonObject jo = new JsonObject();
        jo.addProperty("botToken", botToken);
        jo.addProperty("shardCount", shardCount);
        jo.add("prefixes", GSON.toJsonTree(prefixes));
        jo.add("ownerIDs", GSON.toJsonTree(ownerIDs));
        jo.add("developerIDs", GSON.toJsonTree(developerIDs));
        jo.add("metadata", GSON.toJsonTree(metadata));
        jo.addProperty("errorLogChannelID", errorLogChannelID);
        JsonObject redis = new JsonObject();
        redis.addProperty("enabled", useRedis);
        redis.addProperty("host", redisHost);
        redis.addProperty("port", redisPort);
        redis.addProperty("password", redisPassword);
        redis.addProperty("timeout", redisTimeout);
        jo.add("redis", redis);
        return jo;
    }

    @Override
    public void read(JsonElement jsonElement) {
        if(jsonElement instanceof JsonObject) {
            JsonObject jo = jsonElement.getAsJsonObject();
            try {
                botToken = jo.get("botToken").getAsString();
                shardCount = jo.get("shardCount").getAsInt();
                prefixes = GSON.fromJson(jo.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
                ownerIDs = GSON.fromJson(jo.get("ownerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
                developerIDs = GSON.fromJson(jo.get("developerIDs"), TypeToken.getParameterized(HashSet.class, Long.class).getType());
                if (jo.has("metadata"))
                    metadata = GSON.fromJson(jo.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, Object.class).getType());
                errorLogChannelID = jo.get("errorLogChannelID").getAsLong();
                JsonObject redis = jo.getAsJsonObject("redis");
                useRedis = redis.get("enabled").getAsBoolean();
                redisHost = redis.get("host").getAsString();
                redisPort = redis.get("port").getAsInt();
                redisPassword = redis.get("password").getAsString();
                redisTimeout = redis.get("timeout").getAsInt();
            } catch (Exception e) {
                LOGGER.error("Something went wrong while reading the configuration.", e);
            }
        }
    }

    public BotConfig load() {
        ConfigurationUtils.load("config.json", new File(System.getProperty("user.dir")), this);
        return this;
    }

    public BotConfig save() {
        ConfigurationUtils.saveData("config.json", new File(System.getProperty("user.dir")), this);
        return this;
    }

    public String getBotToken() {
        return botToken;
    }

    public int getShardCount() {
        return shardCount;
    }

    public Set<String> getPrefixes() {
        return new HashSet<>(prefixes);
    }

    public Set<Long> getOwnerIDs() {
        return new HashSet<>(ownerIDs);
    }

    public Set<Long> getDeveloperIDs() {
        return new HashSet<>(developerIDs);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addDeveloper(Long id) {
        developerIDs.add(id);
        save();
    }

    public Set<Long> getBlacklistedIDs() {
        return new HashSet<>(blacklistedIDs);
    }

    public void addBlacklistedID(Long id) {
        blacklistedIDs.add(id);
        save();
    }

    public Long getErrorLogChannelID() {
        return errorLogChannelID;
    }

    public void setErrorLogChannelID(Long id) {
        errorLogChannelID = id;
        save();
    }

    public TextChannel getErrorLogChannel() {
        if(errorLogChannel == null)
            errorLogChannel = bot.getShardManager().getTextChannelByID(errorLogChannelID);

        return errorLogChannel;
    }

    public boolean isUseRedis() {
        return useRedis;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisTimeout() {
        return redisTimeout;
    }
}
