package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.TrackScheduler;
import lavalink.client.io.filters.Filters;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;

@Getter
public class PlayerWrapper implements IPlayerEventListener {

    private final ReentrantLock lock = new ReentrantLock();
    private final LavalinkPlayer internalPlayer;
    private PlayerStatus status;
    private TrackScheduler scheduler;

    public PlayerWrapper(LavalinkPlayer internalPlayer) {
        this.internalPlayer = internalPlayer;
        this.scheduler = new TrackScheduler(this);
        internalPlayer.addListener(this);
    }

    public synchronized void playTrack(AudioTrack track) {
        status = PlayerStatus.REQUESTED;
        CompletableFuture.runAsync(() -> internalPlayer.playTrack(track));
        try {
//            await().atMost(30, TimeUnit.SECONDS).until(() -> status == PlayerStatus.PLAYING || status == PlayerStatus.STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(status);
        }
    }

    public synchronized void stopTrack() {
        if (internalPlayer.getPlayingTrack() == null)
            return;
        status = PlayerStatus.STOPPING;
        CompletableFuture.runAsync(internalPlayer::stopTrack);
        try {
//            await().atMost(30, TimeUnit.SECONDS).until(() -> status == PlayerStatus.STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(status);
        }
    }

    public JdaLink getLink() {
        return ArmaAudio.get().getLavalink().getLink(internalPlayer.getLink().getGuildId());
    }

    public Filters getFilters() {
        return internalPlayer.getFilters();
    }

    public void seekTo(long position) {
        if (status == PlayerStatus.PLAYING)
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
        scheduler.onEvent(event);
    }

    public enum PlayerStatus {
        REQUESTED,
        PLAYING,
        STOPPING,
        STOPPED
    }
}
