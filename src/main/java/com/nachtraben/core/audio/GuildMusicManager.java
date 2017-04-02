package com.nachtraben.core.audio;

import com.nachtraben.core.managers.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

/**
 * Created by NachtRaben on 2/21/2017.
 */
public class GuildMusicManager {

    public static AudioPlayerManager DEFAULT_PLAYER_MANAGER;

    static {
        DEFAULT_PLAYER_MANAGER = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(DEFAULT_PLAYER_MANAGER);
        DEFAULT_PLAYER_MANAGER.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        DEFAULT_PLAYER_MANAGER.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
    }

    private GuildManager guildManager;
    private AudioPlayer player;
    private TrackScheduler scheduler;

    public GuildMusicManager(GuildManager guildManager, AudioPlayerManager playerManager) {
    	this.guildManager = guildManager;
        player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

	public GuildManager getGuildManager() {
		return guildManager;
	}

	public void setTrackScheduler(TrackScheduler scheduler) {
        player.removeListener(this.scheduler);
        this.scheduler = scheduler;
        player.addListener(this.scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

}
