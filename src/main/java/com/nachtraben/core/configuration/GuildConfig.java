package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuildConfig implements CustomJsonIO {

    private final transient static Logger LOGGER = LoggerFactory.getLogger(GuildConfig.class);
    public transient static final File GUILD_DIR = new File("guilds");
    public transient static final File PERSIST_DIR = new File("persists");
    protected transient static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static {
        if (!GUILD_DIR.exists())
            GUILD_DIR.mkdirs();
        if (!PERSIST_DIR.exists())
            PERSIST_DIR.mkdirs();
    }

    private final transient GuildManager guildManager;
    private transient GuildMusicManager musicManager;
    private final transient File configFile;
    private final transient Long guildID;
    private transient Map<ChannelTarget, TextChannel> channelCache;

    boolean deleteCommands = false;

    Set<String> prefixes;
    Map<String, Set<Long>> disabledCommands;
    boolean isBlacklist = true;
    Set<Long> blacklistedIDs;
    Map<String, Long> logChannels;
    Map<String, String> metadata;
    long cooldown;

    public GuildConfig(GuildManager manager, Long guild) {
        this.guildManager = manager;
        this.guildID = guild;
        this.prefixes = new HashSet<>();
        this.disabledCommands = new HashMap<>();
        this.isBlacklist = true;
        this.blacklistedIDs = new HashSet<>();
        this.logChannels = new HashMap<>();
        this.configFile = new File(GUILD_DIR, guild + ".json");
        this.cooldown = -1;
    }

    @Override
    public JsonElement write() {
        return GSON.toJsonTree(this);
    }

    @Override
    public void read(JsonElement jsonElement) {
        if (jsonElement instanceof JsonObject) {
            preInit();
            JsonObject jo = jsonElement.getAsJsonObject();
            if (jo.has("deleteCommands"))
                deleteCommands = jo.get("deleteCommands").getAsBoolean();
            if (jo.has("prefixes"))
                prefixes = GSON.fromJson(jo.get("prefixes"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if (jo.has("disabledCommands"))
                disabledCommands = GSON.fromJson(jo.get("disabledCommands"), new TypeToken<HashMap<String, Set<Long>>>(){}.getType());
            if(jo.has("isBlacklist"))
                isBlacklist = jo.get("isBlacklist").getAsBoolean();
            if (jo.has("blacklistedIDs"))
                blacklistedIDs = GSON.fromJson(jo.get("blacklistedIDS"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if (jo.has("logChannels"))
                logChannels = GSON.fromJson(jo.get("logChannels"), TypeToken.getParameterized(HashMap.class, String.class, Long.class).getType());
            if (jo.has("genericLogChannelID"))
                logChannels.put(ChannelTarget.GENERIC.toString().toLowerCase(), jo.get("genericLogChannelID").getAsLong());
            if (jo.has("musicLogChannelID"))
                logChannels.put(ChannelTarget.MUSIC.toString().toLowerCase(), jo.get("musicLogChannelID").getAsLong());
            if (jo.has("metadata"))
                metadata = GSON.fromJson(jo.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
            if(jo.has("cooldown"))
                cooldown = jo.get("cooldown").getAsLong();
            postInit();
        }
    }

    public GuildConfig load() {
        preInit();
        ConfigurationUtils.load(guildID + ".json", GUILD_DIR, this);
        postInit();
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
        return getMusicManager(true);
    }

    public GuildMusicManager getMusicManager(boolean instantiate) {
        if (musicManager == null && instantiate)
            return musicManager = new GuildMusicManager(getGuild());
        return musicManager;
    }

    public Long getGuildId() {
        return guildID;
    }

    public Guild getGuild() {
        return Armadeus.getInstance().getShardManager().getGuildById(guildID);
    }

    public Map<ChannelTarget, TextChannel> getChannelCache() {
        return channelCache;
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

    public HashMap<String, Set<Long>> getDisabledCommands() {
        return new HashMap<>(disabledCommands);
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public void setBlacklist(boolean blacklist) {
        isBlacklist = blacklist;
    }

    public void setDisabledCommands(Map<String, Set<Long>> commands) {
        this.disabledCommands = commands;
    }

    public void addDisabledCommand(String command, long groupId) {
        disabledCommands.computeIfAbsent(command, set -> new HashSet<>()).add(groupId);
    }

    public void removeDisabledCommand(String command, long groupId) {
        if(disabledCommands.containsKey(command)) {
            Set<Long> ids = disabledCommands.get(command);
            ids.remove(groupId);
            if(ids.isEmpty())
                disabledCommands.remove(command);
        }
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

    public Map<String, Long> getLogChannels() {
        return logChannels;
    }

    public TextChannel getLogChannel(ChannelTarget target) {
        if (channelCache.containsKey(target)) {
            TextChannel channel = channelCache.get(target);
            if (channel == null) {
                long channelID = logChannels.get(target.toString().toLowerCase());
                if (channelID == 0) {
                    return null;
                } else {
                    channel = guildManager.getDbot().getShardManager().getTextChannelById(channelID);
                    if (channel == null) {
                        logChannels.put(target.toString().toLowerCase(), 0L);
                        save();
                        return null;
                    } else {
                        channelCache.put(target, channel);
                    }
                }
            }
            return channel;
        } else if (logChannels.containsKey(target.toString().toLowerCase())) {
            long channelID = logChannels.get(target.toString().toLowerCase());
            if (channelID != 0) {
                TextChannel channel = guildManager.getDbot().getShardManager().getTextChannelById(channelID);
                if (channel != null) {
                    channelCache.put(target, channel);
                    return channel;
                }
                logChannels.put(target.toString().toLowerCase(), 0L);
            }
        }
        return null;
    }

    public void setLogChannel(ChannelTarget target, TextChannel channel) {
        if (channel == null) {
            logChannels.remove(target.toString().toLowerCase());
            channelCache.remove(target);
        } else {
            logChannels.put(target.toString().toLowerCase(), channel.getIdLong());
            channelCache.put(target, channel);
        }
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean hasCooldown() {
        return cooldown > 0;
    }

    public File getConfigFile() {
        return configFile;
    }

    protected void preInit() {
        if (logChannels == null)
            logChannels = new HashMap<>();
        if (channelCache == null)
            channelCache = new HashMap<>();
        if (metadata == null)
            metadata = new HashMap<>();
    }

    protected void postInit() {
        if (metadata.containsKey("volume")) {
            try {
                int volume = Integer.parseInt(metadata.get("volume"));
                volume = Math.min(Math.max(1, volume), 100);
                getMusicManager().getLink().getPlayer().getFilters().setVolume(100.0f / volume).commit();
                LOGGER.info("Setting resume volume of { " + getGuild().getName() + " } to " + volume + ".");

            } catch (NumberFormatException e) {
                metadata.remove("volume");
                save();
            }
        }
    }
}
