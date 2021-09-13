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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayerWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PlayerWrapper.class);
    private final AudioManager manager;
    private final LavalinkPlayer internalPlayer;
    private TrackScheduler scheduler;
    float[] bands = new float[]{ 0.075f, 0.0375f, 0.03f, 0.022499999f, 0.0f, -0.015f, -0.022499999f, -0.0375f, -0.022499999f, -0.015f, 0.0f, 0.022499999f, 0.03f, 0.0375f, 0.075f };


    public PlayerWrapper(AudioManager manager, LavalinkPlayer internalPlayer) {
        this.manager = manager;
        this.internalPlayer = internalPlayer;
        this.scheduler = new TrackScheduler(this);
        internalPlayer.addListener(scheduler);
    }

    public void init() {
        internalPlayer.setVolume((int) (getVolume() * 100.0f)); // Bug in lavalink, this sets initial volume state since filters don't take effect initially
        Filters filters = internalPlayer.getFilters();
        filters = filters.setVolume(getVolume());
        for (int i = 0; i < this.bands.length; i++) {
            filters = filters.setBand(i, this.bands[i] * 1.5f);
        }
        filters.commit();
        logger.info("Setting initial volume for {} to {}", manager.getGuild().getName(),manager.getAudioConfig().get("volume"));
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
        JdaLink link = ArmaAudio.get().getLavalink().getExistingLink(manager.getGuild());
        if(link == null) {
            logger.warn("Creating new link for {}", manager.getGuild().getName());
            link = ArmaAudio.get().getLavalink().getLink(manager.getGuild());
            init();
        }
        return link;
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
