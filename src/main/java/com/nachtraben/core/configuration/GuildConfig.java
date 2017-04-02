package com.nachtraben.core.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.managers.GuildManager;
import net.dv8tion.jda.core.entities.TextChannel;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by NachtRaben on 2/20/2017.
 */
public class GuildConfig implements JsonIO {

	private static HashSet<String> LOADED_CONFIGS = new HashSet<>();

    // Configuration objects
	private String adminLogChannelId;
	private String genericLogChannelId;
	private String musicLogChannelId;

	// Custom prefixes
	private List<String> guildPrefixes;

	private boolean deleteCommandMessages = false;

    private Map<String, Object> metadata;

    // Config
    private String id;
    private TextChannel adminLogChannel;
    private TextChannel genericLogChannel;
    private TextChannel musicLogChannel;

    public GuildConfig(String id) {
    	if(LOADED_CONFIGS.contains(id)) throw new RuntimeException("Attempted to load GuildConfig for { " + id + " } but it was already loaded!");
        this.id = id;
        guildPrefixes = new ArrayList<>();
        metadata = new HashMap<>();
        LOADED_CONFIGS.add(id);
    }

    @Override
    public JsonElement write() {
        JsonObject jo = new JsonObject();
        jo.addProperty("adminLogChannelId", adminLogChannelId);
        jo.addProperty("genericLogChannelId", genericLogChannelId);
        jo.addProperty("musicLogChannelId", musicLogChannelId);
        jo.addProperty("deleteCommandMessages", deleteCommandMessages);
        jo.add("guildPrefixes", JsonLoader.GSON_P.toJsonTree(guildPrefixes));
        jo.add("metadata", JsonLoader.GSON_P.toJsonTree(metadata));
        return jo;
    }

    @Override
    public void read(JsonElement me) {
        if(me instanceof JsonObject) {
            JsonObject jo = me.getAsJsonObject();
            if(jo.has("adminLogChannelId"))
            	adminLogChannelId = jo.get("adminLogChannelId").getAsString();
            if(jo.has("genericLogChannelId"))
            	genericLogChannelId = jo.get("genericLogChannelId").getAsString();
            if(jo.has("musicLogChannelId"))
            	musicLogChannelId = jo.get("musicLogChannelId").getAsString();
            if(jo.has("deleteCommandMessages"))
            	deleteCommandMessages = jo.get("deleteCommandMessages").getAsBoolean();
            Type type = new TypeToken<List<String>>(){}.getType();
            if(jo.has("guildPrefixes"))
            	guildPrefixes = JsonLoader.GSON_P.fromJson(jo.get("guildPrefixes"), type);
            type = new TypeToken<Map<String, Object>>(){}.getType();
            if(jo.has("metadata"))
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

    /* Convenience getters for logging channels */
    public TextChannel getAdminLogChannel() {
    	if(adminLogChannel == null)
    		adminLogChannel = GuildManager.getManagerFor(id).getGuild().getTextChannelById(adminLogChannelId);
    	return adminLogChannel;
	}

	public void setAdminLogChannel(TextChannel channel) {
    	if(channel == null) {
    		adminLogChannel = null;
    		adminLogChannelId = null;
		} else {
			adminLogChannel = channel;
			adminLogChannelId = channel.getId();
		}
		save();
	}

	public TextChannel getGenericLogChannel() {
		if(genericLogChannel == null)
			genericLogChannel = GuildManager.getManagerFor(id).getGuild().getTextChannelById(genericLogChannelId);
		return genericLogChannel;
	}

	public void setGenericLogChannel(TextChannel channel) {
    	if(channel == null) {
    		genericLogChannel = null;
    		genericLogChannelId = null;
		} else {
			genericLogChannel = channel;
			genericLogChannelId = channel.getId();
		}
    	save();
	}

	public TextChannel getMusicLogChannel() {
		if(musicLogChannel == null)
			musicLogChannel = GuildManager.getManagerFor(id).getGuild().getTextChannelById(musicLogChannelId);
		return musicLogChannel;
	}

	public void setMusicLogChannel(TextChannel channel) {
    	if(channel == null) {
    		musicLogChannel = null;
    		musicLogChannelId = null;
		} else {
			musicLogChannel = channel;
			musicLogChannelId = channel.getId();
		}
    	save();
	}

	public boolean shouldDeleteCommandMessages() {
    	return deleteCommandMessages;
	}

	public void setDeleteCommandMessages(boolean b) {
    	deleteCommandMessages = b;
    	save();
	}

	public void setCommandPrefixes(String[] commandPrefixes) {
		guildPrefixes.clear();
		if(commandPrefixes != null)
			guildPrefixes.addAll(Arrays.asList(commandPrefixes));
		else
			guildPrefixes = null;
		save();
	}

	public List<String> getGuildPrefixes() {
		return new ArrayList<>(guildPrefixes);
	}
}
