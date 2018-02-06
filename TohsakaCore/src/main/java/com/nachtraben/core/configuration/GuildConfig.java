package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.lemonslice.ConfigurationUtils;
import com.nachtraben.lemonslice.CustomJsonIO;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GuildConfig implements CustomJsonIO {

    // TODO: Don't hold on to JDA Objects.

    private transient static Logger LOGGER = LoggerFactory.getLogger(GuildConfig.class);
    public transient static final File GUILD_DIR = new File("guilds");
    public transient static final File PERSIST_DIR = new File("persists");
    protected transient static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static {
        if (!GUILD_DIR.exists())
            GUILD_DIR.mkdirs();
        if (!PERSIST_DIR.exists())
            PERSIST_DIR.mkdirs();
    }

    private transient GuildManager guildManager;
    private transient GuildMusicManager musicManager;
    private transient File configFile;
    private transient Long guildID;
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
        ConfigurationUtils.load(guildID + ".json", GUILD_DIR, this);
        return this;
    }

    public GuildConfig save() {
        ConfigurationUtils.saveData(guildID + ".json", GUILD_DIR, this);
        return this;
    }

    public void savePersistInfo() {
        GuildMusicManager music = getMusicManager(false);
        if (music != null && music.getScheduler().isPersist() && music.getScheduler().isPlaying()) {
            LOGGER.debug("Saving persist info for: " + getGuildID());
            TrackScheduler sched = music.getScheduler();
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(PERSIST_DIR, getGuildID() + ".persist")))) {
                /*
                    isPersist
                    isRepeatTrack
                    isRepeatQueue
                    channelID
                    tracks
                 */
                out.writeBoolean(sched.isPersist());
                out.writeBoolean(sched.isRepeatTrack());
                out.writeBoolean(sched.isRepeatQueue());
                out.writeLong(getGuild().getAudioManager().isConnected() ? getGuild().getAudioManager().getConnectedChannel().getIdLong() : -1L);
                List<AudioTrack> tracks = new ArrayList<>();
                tracks.add(getMusicManager().getPlayer().getPlayingTrack());
                tracks.addAll(sched.getQueue());
                MessageOutput mout = new MessageOutput(out);
                int count = 0;
                for (AudioTrack track : tracks) {
                    if(track == null) {
                        LOGGER.error("Wtf, null track in `" + getGuild().getName() + "`. O.o");
                        continue;
                    }
                    AudioSourceManager sm = track.getSourceManager();
                    if(sm == null) {
                        LOGGER.debug("Invalid source manager in " + getGuild().getName() + "?");
                        LOGGER.debug(track.getIdentifier());
                        LOGGER.debug(String.valueOf(track.getInfo()));
                        LOGGER.debug(String.valueOf(track.getState()));
                        LOGGER.debug(String.valueOf(track.getUserData()));
                    } else if (track.getSourceManager().isTrackEncodable(track)) {
                        getMusicManager().getPlayerManager().encodeTrack(mout, track);
                        byte[] userdata = Utils.serialize(track.getUserData(GuildCommandSender.class));
                        out.writeInt(userdata.length);
                        out.write(userdata);
                        count++;
                    } else {
                        LOGGER.debug("Can't encode track: " + track.getIdentifier());
                    }
                }
                LOGGER.debug("Encoded " + count + " tracks.");
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void loadPersistInfo(File f) {
        LOGGER.debug("Loading persist info for: " + getGuildID());
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            TrackScheduler sched = getMusicManager().getScheduler();
            sched.setPersist(in.readBoolean());
            sched.setRepeatTrack(in.readBoolean());
            sched.setRepeatQueue(in.readBoolean());
            try {
                Long channel = in.readLong();
                if(channel == -1) {
                    LOGGER.debug("Invalid voice channel, returning.");
                    f.delete();
                    return;
                }
                getGuild().getAudioManager().setSelfDeafened(true);
                getGuild().getAudioManager().openAudioConnection(getGuild().getVoiceChannelById(channel));
                while(!getGuild().getAudioManager().isConnected()) {
                    Thread.sleep(100);
                }
                VoiceChannel v = getGuild().getAudioManager().getConnectedChannel();
                LOGGER.debug("Successfully joined: " + getGuild().getName() + ">>" + v.getName());
            } catch (Exception e) {
                LOGGER.debug("Failed to join voice channel.");
                f.delete();
                return;
            }
            Thread.sleep(1000);
            MessageInput mi = new MessageInput(in);
            AudioPlayerManager manager = getMusicManager().getPlayerManager();
            DecodedTrackHolder holder;
            int count = 0;
            try {
                while ((holder = manager.decodeTrack(mi)) != null) {
                    AudioTrack track = holder.decodedTrack;
                    byte[] userdata = new byte[in.readInt()];
                    in.read(userdata);
                    GuildCommandSender sender = Utils.deserialize(userdata, GuildCommandSender.class);
                    sender.build(Tohsaka.getInstance());
                    track.setUserData(sender);
                    sched.queue(track);
                    count++;
                }
            } catch (EOFException ignored) {}
            LOGGER.debug("Decoded " + count + " tracks.");
        } catch (Exception e) {
            LOGGER.warn("Failed to resume persist state for, " + getGuildID() + ".", e);
        }
        f.delete();
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildMusicManager getMusicManager() {
        return getMusicManager(true);
    }

    public GuildMusicManager getMusicManager(boolean instantiate) {
        LOGGER.debug("Getting manager for " + guildID);
        if (musicManager == null && instantiate)
            return musicManager = new GuildMusicManager(getGuild(), GuildMusicManager.DEFAULT_PLAYER_MANAGER);

        return musicManager;
    }

    public Long getGuildID() {
        return guildID;
    }

    public Guild getGuild() {
        return Tohsaka.getInstance().getShardManager().getGuildByID(guildID);
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
                    channel = guildManager.getDbot().getShardManager().getTextChannelByID(channelID);
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
                TextChannel channel = guildManager.getDbot().getShardManager().getTextChannelByID(channelID);
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
                volume = Math.min(Math.max(volume, 0), 150);
                getMusicManager().getPlayer().setVolume(volume);
                LOGGER.info("Setting resume volume of { " + getGuild().getName() + " } to " + volume + ".");
            } catch (NumberFormatException e) {
                metadata.remove("volume");
                save();
            }
        }
    }
}
