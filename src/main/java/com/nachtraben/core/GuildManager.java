package com.nachtraben.core;

import com.nachtraben.core.audio.GuildMusicManager;
import com.nachtraben.core.configuration.GuildConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.entities.Guild;

import java.util.HashMap;

/**
 * Created by NachtRaben on 2/20/2017.
 */
public class GuildManager {

    private static HashMap<String, GuildManager> GUILDS;
    public static AudioPlayerManager DEFAULT_PLAYER_MANAGER;

    static {
        GUILDS = new HashMap<>();
        DEFAULT_PLAYER_MANAGER = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(DEFAULT_PLAYER_MANAGER);
        DEFAULT_PLAYER_MANAGER.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        DEFAULT_PLAYER_MANAGER.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        DEFAULT_PLAYER_MANAGER.source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    private String id;
    private GuildConfig config;
    private GuildMusicManager audioManager;

    public GuildManager(Guild guild) {
        if(guild == null) throw new IllegalArgumentException("Guild cannot be null!");
        this.id = guild.getId();
        config = new GuildConfig(id).load();
    }

    private Guild getGuild() {
        return JDABot.getInstance().getGuildById(String.valueOf(id));
    }

    public GuildConfig getConfig() {
        return this.config;
    }

    public GuildMusicManager getAudioManager() {
        if(audioManager == null)
            setAudioManager(new GuildMusicManager(DEFAULT_PLAYER_MANAGER));
        return audioManager;
    }

    public void setAudioManager(GuildMusicManager manager) {
        if(audioManager != null)
            audioManager.getPlayer().destroy();
        audioManager = manager;
        getGuild().getAudioManager().setSendingHandler(manager.getSendHandler());
    }

    public static GuildManager createManagerFor(Guild g) {
        GuildManager manager = new GuildManager(g);
        GUILDS.put(g.getId(), manager);
        return manager;
    }

    public static GuildManager getManagerFor(String id) {
        return GUILDS.get(id);
    }

}
