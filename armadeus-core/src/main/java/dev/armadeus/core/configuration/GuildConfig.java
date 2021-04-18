package dev.armadeus.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.managers.GuildManager;
import dev.armadeus.core.managers.GuildMusicManager;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class GuildConfig implements CustomJsonIO {

    private final transient static Logger logger = LogManager.getLogger();
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
    private transient int messageTimeout;

    boolean deleteCommands = false;

    Set<String> prefixes;
    Map<String, Set<Long>> disabledCommands;
    boolean isBlacklist = true;
    Set<Long> blacklistedIDs;
    Map<String, String> metadata;
    long cooldown;

    public GuildConfig(GuildManager manager, Long guild) {
        this.guildManager = manager;
        this.guildID = guild;
        this.prefixes = new HashSet<>();
        this.disabledCommands = new HashMap<>();
        this.isBlacklist = true;
        this.blacklistedIDs = new HashSet<>();
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
                disabledCommands = GSON.fromJson(jo.get("disabledCommands"), new TypeToken<HashMap<String, Set<Long>>>() {
                }.getType());
            if (jo.has("isBlacklist"))
                isBlacklist = jo.get("isBlacklist").getAsBoolean();
            if (jo.has("blacklistedIDs"))
                blacklistedIDs = GSON.fromJson(jo.get("blacklistedIDS"), TypeToken.getParameterized(HashSet.class, String.class).getType());
            if (jo.has("metadata"))
                metadata = GSON.fromJson(jo.get("metadata"), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
            if (jo.has("cooldown"))
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
        return DiscordBot.get().getShardManager().getGuildById(guildID);
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
        if (disabledCommands.containsKey(command)) {
            Set<Long> ids = disabledCommands.get(command);
            ids.remove(groupId);
            if (ids.isEmpty())
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
    }

    protected void postInit() {
        if (metadata.containsKey("volume")) {
            try {
                int volume = Integer.parseInt(metadata.get("volume"));
                volume = Math.min(Math.max(1, volume), 100);
                getMusicManager().getLink().getPlayer().getFilters().setVolume(volume / 100.0f).commit();
                logger.info("Setting resume volume of { " + getGuild().getName() + " } to " + volume + ".");

            } catch (NumberFormatException e) {
                metadata.remove("volume");
                save();
            }
        }
    }
}