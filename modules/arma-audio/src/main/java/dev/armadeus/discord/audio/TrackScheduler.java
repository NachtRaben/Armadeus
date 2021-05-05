package dev.armadeus.discord.audio;

import com.google.common.base.MoreObjects;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler extends AudioEventAdapter implements IPlayerEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final AudioManager manager;

    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack lastTrack;
    private AudioTrack currentTrack;

    private boolean repeatTrack;
    private boolean repeatQueue;

    public TrackScheduler(AudioManager audioManager) {
        this.manager = audioManager;
        queue = new LinkedBlockingDeque<>();
    }

    public void queue(AudioTrack track) {
        synchronized (queue) {
            synchronized (queue) {
                queue.offer(track);
            }
            if (!isPlaying()) skip();
        }
    }

    public void play(AudioTrack track) {
        logger.info("Playing track {} in {}", track.getInfo().title, manager.getConfig().getGuild().getName());
        DiscordCommandIssuer user = track.getUserData(DiscordCommandIssuer.class);

        // No requester set or banned
        if (user == null) {
            logger.warn("Null track requester");
            skip();
            return;
        }

        // Member left or banned
        if (user.getMember() == null) {
            skip();
            return;
        }

        if (!joinVoiceChannel(user)) {
            stop();
            return;
        }
        sendEmbed(track, user);
        long start = System.currentTimeMillis();
        manager.getPlayer().playTrack(track);
        logger.warn("Took {} ms to play track", System.currentTimeMillis() - start);
    }

    public void stop() {
        synchronized (queue) {
            repeatTrack = false;
            repeatQueue = false;
            manager.getPlayer().setPaused(false);
            queue.clear();
            if (isPlaying()) {
                long start = System.currentTimeMillis();
                manager.getPlayer().stopTrack();
                logger.warn("Took {} ms to stop track", System.currentTimeMillis() - start);
            }
            currentTrack = null;
        }
    }

    public void skip() {
        synchronized (queue) {
            repeatTrack = false;
            lastTrack = currentTrack;
            if (currentTrack != null && repeatQueue)
                queue.addLast(getCurrentTrack());
            if (queue.isEmpty() && isPlaying()) {
                stop();
            } else if (!queue.isEmpty()) {
                play(queue.poll());
            } else {
            }
        }
    }

    public boolean shuffle() {
        synchronized (queue) {
            if (!queue.isEmpty()) {
                List<AudioTrack> tracks = new ArrayList<>();
                queue.drainTo(tracks);
                Collections.shuffle(tracks);
                queue.addAll(tracks);
                return true;
            }
            return false;
        }
    }

    public boolean isPlaying() {
        return currentTrack != null;
    }

    public boolean joinVoiceChannel(DiscordCommandIssuer user) {
        long connected = manager.getLink().getChannelId();
        VoiceChannel userChannel = user.getVoiceChannel();
        if (connected != -1 && isPlaying()) {
            return true;
        }
        if (userChannel != null) {
            try {
                manager.getLink().connect(userChannel);
                logger.info("Connecting to {} in {}", userChannel.getName(), userChannel.getGuild().getName());
                return true;
            } catch (InsufficientPermissionException e) {
                user.sendMessage(String.join("\n", "Failed to join your channel because of missing permission `" + e.getPermission() + "`",
                        "This may be due to a discord bug, please ensure the bot is an `Administrator` or add a channel specific override for `VOICE_CONNECT` and `VOICE_MOVE_OTHERS`"));
            }
        }
        return false;
    }

    public void onTrackEnd(TrackEndEvent event) {
        IPlayer player = event.getPlayer();
        AudioTrackEndReason endReason = event.getReason();
        if (endReason.mayStartNext && repeatTrack) {
            player.playTrack(getCurrentTrack());
        } else if (endReason.mayStartNext) {
            skip();
        } else {
            stop();
        }
    }

    public void onTrackException(TrackExceptionEvent event) {
        AudioTrack track = event.getTrack();
        Exception exception = event.getException();
        if (exception == null) {
            logger.warn("Null exception in audio manager");
            return;
        }
        if (repeatTrack) {
            repeatTrack = false;
        }
        if (exception.getCause().getMessage().toLowerCase().contains("read timed out")) {
            queue.addFirst(event.getTrack().makeClone());
            return;
        }
        logger.warn(event.getTrack().toString());
        logger.warn(event.getException().toString());
        Collection<ScheduledTask> future = manager.listeners.values();
        future.forEach(ScheduledTask::cancel);
        manager.listeners.clear();
        DiscordCommandIssuer requester = event.getTrack().getUserData(DiscordCommandIssuer.class);
        requester.sendMessage(String.format("Failed to play `%s` because, `%s`", track.getInfo().title, exception.getMessage()));
        logger.warn("Something went wrong with lavaplayer.", exception);
    }

    public void onTrackStuck(TrackStuckEvent event) {
        AudioTrack track = event.getTrack();
        DiscordCommandIssuer requestor = track.getUserData(DiscordCommandIssuer.class);
        requestor.sendMessage("Got stuck while playing `" + track.getInfo().title + "`. It will be skipped.");
        skip();
    }

    private void sendEmbed(AudioTrack track, DiscordCommandIssuer sender) {
        EmbedBuilder builder = AudioEmbedUtils.getNowPlayingEmbed(sender, track);
        sender.sendMessage(builder.build());
    }

    public List<AudioTrack> getQueue() {
        List<AudioTrack> tracks;
        synchronized (queue) {
            tracks = new ArrayList<>(queue);
        }
        return tracks;
    }

    public void skipTo(AudioTrack track) {
        synchronized (queue) {
            if (queue.contains(track)) {
                while (queue.peek() != track) {
                    if (repeatQueue)
                        queue.addLast(queue.pop());
                    else
                        queue.pop();
                }
                skip();
            }
        }
    }

    public AudioTrack getLastTrack() {
        if (lastTrack != null) {
            AudioTrack track = lastTrack.makeClone();
            track.setUserData(lastTrack.getUserData());
            return track;
        }
        return null;
    }

    public AudioTrack getCurrentTrack() {
        if (currentTrack != null) {
            AudioTrack track = currentTrack.makeClone();
            track.setUserData(currentTrack.getUserData());
            return track;
        }
        return null;
    }

    public boolean isRepeatTrack() {
        return repeatTrack;
    }

    public void setRepeatTrack(boolean repeatTrack) {
        this.repeatTrack = repeatTrack;
    }

    public boolean isRepeatQueue() {
        return repeatQueue;
    }

    public void setRepeatQueue(boolean repeatQueue) {
        this.repeatQueue = repeatQueue;
    }

    @Override
    public void onEvent(PlayerEvent event) {
        if (event instanceof TrackStartEvent) {
            onTrackStart((TrackStartEvent) event);
        } else if (event instanceof TrackEndEvent) {
            onTrackEnd((TrackEndEvent) event);
        } else if (event instanceof TrackExceptionEvent) {
            onTrackException((TrackExceptionEvent) event);
        } else if (event instanceof TrackStuckEvent) {
            onTrackStuck((TrackStuckEvent) event);
        }
    }

    private void onTrackStart(TrackStartEvent event) {
        currentTrack = event.getTrack();
    }

}