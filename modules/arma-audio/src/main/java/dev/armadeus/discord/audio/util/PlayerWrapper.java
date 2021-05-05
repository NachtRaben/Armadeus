package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import lavalink.client.io.filters.Filters;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.*;
import lombok.Getter;

@Getter
public class PlayerWrapper implements IPlayer, IPlayerEventListener {

    private final LavalinkPlayer player;
    private PlayerStatus status;

    public PlayerWrapper(LavalinkPlayer player) {
        this.player = player;
        player.addListener(this);
    }


    public void playTrack(AudioTrack track) {
        synchronized (player) {
            status = PlayerStatus.REQUESTED;
            player.playTrack(track);
            while (status != PlayerStatus.PLAYING && status != PlayerStatus.STOPPED) {
                try {
                    player.wait(50);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void stopTrack() {
        if(player.getPlayingTrack() == null)
            return;
        synchronized (player) {
            status = PlayerStatus.STOPPING;
            player.stopTrack();
            while(status != PlayerStatus.STOPPED) {
                try {
                    player.wait(50);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public Link getLink() {
        return player.getLink();
    }

    public Filters getFilters() {
        return player.getFilters();
    }

    public void addListener(IPlayerEventListener listener) {
        player.addListener(listener);
    }

    @Override
    public void removeListener(IPlayerEventListener listener) {
        player.removeListener(listener);
    }

    public void seekTo(long position) {
        player.seekTo(position);
    }

    @Override
    public void setVolume(int volume) {
        player.getFilters().setVolume(Math.max(Math.min(volume / 100.0f, 0.0f), 1.0f)).commit();
    }

    @Override
    public int getVolume() {
        return player.getVolume();
    }

    public void setPaused(boolean paused) {
        player.setPaused(paused);
    }

    @Override
    public boolean isPaused() {
        return player.isPaused();
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public long getTrackPosition() {
        return player.getTrackPosition();
    }

    @Override
    public void onEvent(PlayerEvent event) {
        synchronized (player) {
            if (event instanceof TrackStartEvent) {
                status = PlayerStatus.PLAYING;
                player.notifyAll();
            } else if (event instanceof TrackEndEvent) {
                status = PlayerStatus.STOPPED;
                player.notifyAll();
            } else if (event instanceof TrackStuckEvent) {
                status = PlayerStatus.STOPPED;
                player.notifyAll();
            } else if (event instanceof TrackExceptionEvent) {
                status = PlayerStatus.STOPPED;
                player.notifyAll();
            }
        }
    }

    public enum PlayerStatus {
        REQUESTED,
        PLAYING,
        STOPPING,
        STOPPED
    }

}
