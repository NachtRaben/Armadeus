package com.nachtraben.core.managers;

import com.nachtraben.core.audio.AudioPlayerSendHandler;
import com.nachtraben.core.audio.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {

    public static AudioPlayerManager DEFAULT_PLAYER_MANAGER;

    static {
        DEFAULT_PLAYER_MANAGER = new DefaultAudioPlayerManager();
        DEFAULT_PLAYER_MANAGER.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        DEFAULT_PLAYER_MANAGER.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        AudioSourceManagers.registerLocalSource(DEFAULT_PLAYER_MANAGER);
        AudioSourceManagers.registerRemoteSources(DEFAULT_PLAYER_MANAGER);
        /*DEFAULT_PLAYER_MANAGER.registerSourceManager(new YoutubeAudioSourceManager(true));
        DEFAULT_PLAYER_MANAGER.registerSourceManager(new SoundCloudAudioSourceManager());
        DEFAULT_PLAYER_MANAGER.registerSourceManager(new VimeoAudioSourceManager());
        DEFAULT_PLAYER_MANAGER.registerSourceManager(new TwitchStreamAudioSourceManager());
        DEFAULT_PLAYER_MANAGER.registerSourceManager(new BeamAudioSourceManager());*/
    }

    private Guild guild;
    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private TrackScheduler scheduler;
    private AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(Guild guild, AudioPlayerManager playerManager) {
        if(guild == null)
            throw new IllegalArgumentException("Provided guild cannot be null.");
        this.guild = guild;
        this.playerManager = playerManager;
        player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
        guild.getAudioManager().setSendingHandler(getSendHandler());
    }

    public Guild getGuild() {
        return guild;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public void setTrackScheduler(TrackScheduler scheduler) {
        this.scheduler.destroy();
        player.removeListener(this.scheduler);
        this.scheduler = scheduler;
        player.addListener(this.scheduler);
    }

    public AudioSendHandler getSendHandler() {
        if(sendHandler == null)
            sendHandler = new AudioPlayerSendHandler(player);
        return sendHandler;
    }

}
