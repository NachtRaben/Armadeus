package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuildConfig implements CustomJsonIO {
    private transient static Logger LOGGER = LoggerFactory.getLogger(GuildConfig.class);
    private transient static final File GUILD_DIR = new File("guilds");
    private transient static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static {
        if(!GUILD_DIR.exists())
            GUILD_DIR.mkdirs();
    }

    transient GuildManager guildManager;
    transient GuildMusicManager musicManager;
    transient File configFile;
    transient Long guildID;
    transient Guild guild;
    transient Map<ChannelTarget, TextChannel> channelCache;
    
    boolean deleteCommands = false;

    Set<String> prefixes;
    Set<String> disabledCommands;
    Set<Long> blacklistedIDs;
    Map<String, Long> logChannels;
    boolean debugging;

    public GuildConfig(GuildManager manager, Long guild) {
        this.guildManager = manager;
        this.guildID = guild;
        this.prefixes = new HashSet<>();
        this.disabledCommands = new HashSet<>();
        this.blacklistedIDs = new HashSet<>();
        this.logChannels = new HashMap<>();
        this.configFile = new File(GUILD_DIR, guild + ".json");
    }

    @Override
    public JsonElement write() {
        return GSON.toJsonTree(this);
    }

    @Override
    public void read(JsonElement jsonElement) {
        if(logChannels == null)
            logChannels = new HashMap<>();
        if(channelCache == null)
            channelCache = new HashMap<>();

        if(jsonElement instanceof JsonObject) {
            JsonObject jo = jsonElement.getAsJsonObject();
            if(jo.has("deleteCommands"))
                deleteCommands = jo.get("deleteCommands").getAsBoolean();
            if(jo.has("prefixes"))
                prefixes = GSON.fromJson(jo.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if(jo.has("disabledCommands"))
                disabledCommands = GSON.fromJson(jo.get("disabledCommands"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if(jo.has("blacklistedIDs"))
                blacklistedIDs = GSON.fromJson(jo.get("blacklistedIDS"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if(jo.has("logChannels"))
                logChannels = GSON.fromJson(jo.get("logChannels"), TypeToken.getParameterized(HashMap.class, String.class, Long.class).getType());
            if(jo.has("genericLogChannelID"))
                logChannels.put(ChannelTarget.GENERIC.toString().toLowerCase(), jo.get("genericLogChannelID").getAsLong());
            if(jo.has("musicLogChannelID"))
                logChannels.put(ChannelTarget.MUSIC.toString().toLowerCase(), jo.get("musicLogChannelID").getAsLong());
        }
    }

    public GuildConfig load() {
        ConfigurationUtils.load(guildID + ".json", GUILD_DIR, this);
        return this;
    }

    public GuildConfig save() {
        ConfigurationUtils.saveData(guildID + ".json", GUILD_DIR, this);
        return this;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildMusicManager getMusicManager() {
        if(musicManager == null)
            musicManager = new GuildMusicManager(getGuild(), GuildMusicManager.DEFAULT_PLAYER_MANAGER);
        return musicManager;
    }

    public Long getGuildID() {
        return guildID;
    }

    public Guild getGuild() {
        if(guild == null)
            guild = Tohsaka.getInstance().getShardManager().getGuildByID(guildID);
        return guild;
    }

    public boolean shouldDeleteCommands() {
        return deleteCommands;
    }
    
    public void setDeleteCommands(boolean delete) {
        deleteCommands = delete;
    }

    public Set<String> getPrefixes() {
        return new HashSet<>(prefixes);
    }

    public void setPrefixes(Set<String> prefixes) {
        this.prefixes = prefixes;
    }

    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    public Set<String> getDisabledCommands() {
        return new HashSet<>(disabledCommands);
    }

    public void setDisabledCommands(Set<String> commands) {
        this.disabledCommands = commands;
    }

    public void addDisabledCommand(String command) {
        disabledCommands.add(command);
    }

    public Set<Long> getBlacklistedIDs() {
        return new HashSet<>(blacklistedIDs);
    }

    public void setBlacklistedIDs(Set<Long> ids) {
        this.blacklistedIDs = ids;
    }

    public void addBlacklistedID(Long id) {
        blacklistedIDs.add(id);
    }

    public Map<String,Long> getLogChannels() {
        return logChannels;
    }

    public TextChannel getLogChannel(ChannelTarget target) {
        if(channelCache.containsKey(target)) {
            TextChannel channel = channelCache.get(target);
            if(channel == null) {
                long channelID = logChannels.get(target.toString().toLowerCase());
                if(channelID == 0) {
                    return null;
                } else {
                    channel = guildManager.getDbot().getShardManager().getTextChannelByID(channelID);
                    if(channel == null) {
                        logChannels.put(target.toString().toLowerCase(), 0L);
                        save();
                        return null;
                    } else {
                        channelCache.put(target, channel);
                    }
                }
            }
            return channel;
        } else if(logChannels.containsKey(target.toString().toLowerCase())) {
            long channelID = logChannels.get(target.toString().toLowerCase());
            if(channelID != 0) {
                TextChannel channel = guildManager.getDbot().getShardManager().getTextChannelByID(channelID);
                if(channel != null) {
                    channelCache.put(target, channel);
                    return channel;
                }
                logChannels.put(target.toString().toLowerCase(), 0L);
            }
        }
        return null;
    }

    public void setLogChannel(ChannelTarget target, TextChannel channel) {
        if(channel == null) {
            logChannels.remove(target.toString().toLowerCase());
            channelCache.remove(target);
        } else {
            logChannels.put(target.toString().toLowerCase(), channel.getIdLong());
            channelCache.put(target, channel);
        }
    }

    public File getConfigFile() {
        return configFile;
    }
}
