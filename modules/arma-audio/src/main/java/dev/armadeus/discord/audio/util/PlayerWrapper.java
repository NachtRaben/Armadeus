package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import lavalink.client.io.filters.Filters;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;

@Getter
public class PlayerWrapper implements IPlayer, IPlayerEventListener {

    private final ReentrantLock lock = new ReentrantLock();
    private final LavalinkPlayer player;
    private PlayerStatus status;

    public PlayerWrapper(LavalinkPlayer player) {
        this.player = player;
        player.addListener(this);
    }

    public synchronized void playTrack(AudioTrack track) {
        status = PlayerStatus.REQUESTED;
        player.playTrack(track);
        try {
            await().atMost(30, TimeUnit.SECONDS).until(() -> status == PlayerStatus.PLAYING || status == PlayerStatus.STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(status);
        }
    }

    public synchronized void stopTrack() {
        if (player.getPlayingTrack() == null)
            return;
        status = PlayerStatus.STOPPING;
        player.stopTrack();
        try {
            await().atMost(30, TimeUnit.SECONDS).until(() -> status == PlayerStatus.STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(status);
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
        if (status == PlayerStatus.PLAYING)
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
            if (event instanceof TrackStartEvent) {
                status = PlayerStatus.PLAYING;
            } else if (event instanceof TrackEndEvent) {
                status = PlayerStatus.STOPPED;
            } else if (event instanceof TrackStuckEvent) {
                status = PlayerStatus.STOPPED;
            } else if (event instanceof TrackExceptionEvent) {
                status = PlayerStatus.STOPPED;
            }
    }

    public enum PlayerStatus {
        REQUESTED,
        PLAYING,
        STOPPING,
        STOPPED
    }
}
