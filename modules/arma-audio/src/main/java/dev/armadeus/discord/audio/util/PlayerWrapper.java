package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.TrackScheduler;
import lavalink.client.io.filters.Filters;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lombok.Getter;

@Getter
public class PlayerWrapper {

    private final AudioManager manager;
    private final LavalinkPlayer internalPlayer;
    private TrackScheduler scheduler;

    public PlayerWrapper(AudioManager manager, LavalinkPlayer internalPlayer) {
        this.manager = manager;
        this.internalPlayer = internalPlayer;
        this.scheduler = new TrackScheduler(this);
        internalPlayer.addListener(scheduler);
    }

    public synchronized void playTrack(AudioTrack track) {
        internalPlayer.playTrack(track);
    }

    public synchronized void stopTrack() {
        if (internalPlayer.getPlayingTrack() == null)
            return;
        internalPlayer.stopTrack();
    }

    public JdaLink getLink() {
        return ArmaAudio.get().getLavalink().getLink(manager.getGuild());
    }

    public Filters getFilters() {
        return internalPlayer.getFilters();
    }

    public void seekTo(long position) {
        if(internalPlayer.getPlayingTrack() != null)
            internalPlayer.seekTo(position);
    }

    public void setVolume(int volume) {
        internalPlayer.getFilters().setVolume(Math.max(Math.min(volume / 100.0f, 0.0f), 1.0f)).commit();
    }

    public int getVolume() {
        return internalPlayer.getVolume();
    }

    public void setPaused(boolean paused) {
        internalPlayer.setPaused(paused);
    }

    public boolean isPaused() {
        return internalPlayer.isPaused();
    }

    public AudioTrack getPlayingTrack() {
        return internalPlayer.getPlayingTrack();
    }

    public long getTrackPosition() {
        return internalPlayer.getTrackPosition();
    }

    public boolean isPlaying() {
        return scheduler.isPlaying();
    }

}
