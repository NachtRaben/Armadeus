package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.TrackScheduler;
import lavalink.client.io.filters.Filters;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayerWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PlayerWrapper.class);
    private final AudioManager manager;
    private final LavalinkPlayer internalPlayer;
    private final TrackScheduler scheduler;


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
        return (JdaLink) internalPlayer.getLink();
    }

    public Filters getFilters() {
        return internalPlayer.getFilters();
    }

    public void seekTo(long position) {
        if(internalPlayer.getPlayingTrack() != null)
            internalPlayer.seekTo(position);
    }

    public void setVolume(int volume) {
        internalPlayer.setVolume(volume);
        setVolume(volume / 100.0f);
    }

    public void setVolume(float vol) {
        vol = (float)Math.min(Math.max(vol, 0.0), 1.0);
        internalPlayer.setVolume((int) (vol * 100.0f));
        internalPlayer.getFilters().setVolume(vol).commit();
        manager.getAudioConfig().set("volume", String.format("%.2f", vol));
    }

    public float getVolume() {
        return manager.getVolume();
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
