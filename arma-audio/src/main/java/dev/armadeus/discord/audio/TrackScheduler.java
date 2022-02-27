package dev.armadeus.discord.audio;

import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import dev.armadeus.discord.audio.util.PlayerWrapper;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class TrackScheduler implements IPlayerEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final PlayerWrapper player;

    private final ReentrantLock lock = new ReentrantLock();
    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack lastTrack;
    private AudioTrack currentTrack;

    @Setter
    private boolean repeatTrack;
    @Setter
    private boolean repeatQueue;

    public TrackScheduler(PlayerWrapper player) {
        this.player = player;
        queue = new LinkedBlockingDeque<>();
    }

    public void queue(AudioTrack track) {
        lock.lock();
        try {
            queue.offer(track);
            if (!isPlaying()) skip();
        } finally {
            lock.unlock();
        }
    }

    public void play(AudioTrack track) {
        logger.info("{} => Playing {}", player.getManager().getGuild().getName(), track.getInfo().getTitle());
        DiscordCommandIssuer user = track.getUserData(DiscordCommandIssuer.class);

        // No requester set or banned
        if (user == null) {
            logger.warn("Failed to play track due to missing user data");
            return;
        }

        // Member left or banned
        if (user.getMember() == null) {
            logger.warn("Failed to play track due to missing member");
            return;
        }

        if (!joinAudioChannel(user)) {
            stop();
            return;
        }

        currentTrack = track;
        sendEmbed(track, user);
        player.playTrack(track);
    }

    public void stop() {
        logger.info("{} => Stopping audio playback", player.getManager().getGuild().getName());
        lock.lock();
//        player.getManager().getListeners().forEach((l, e) -> e.cancel());
//        player.getManager().getListeners().clear();
        try {
            repeatTrack = false;
            repeatQueue = false;
            player.setPaused(false);
            queue.clear();
            if (isPlaying())
                player.stopTrack();
            currentTrack = null;
        } finally {
            lock.unlock();
        }
    }

    public void skip() {
        logger.info("{} => Skipping track", player.getManager().getGuild().getName());
        lock.lock();
        try {
            repeatTrack = false;
            lastTrack = currentTrack;
            if (currentTrack != null && repeatQueue)
                queue.addLast(getCurrentTrack());
            if (queue.isEmpty() && isPlaying()) {
                logger.info("{} => Stopping, empty queue", player.getManager().getGuild().getName());
                stop();
            } else if (!queue.isEmpty()) {
                play(queue.poll());
            }
        } finally {
            lock.unlock();
        }
    }

    public void shuffle() {
        lock.lock();
        try {
            if (!queue.isEmpty()) {
                List<AudioTrack> tracks = new ArrayList<>();
                queue.drainTo(tracks);
                Collections.shuffle(tracks);
                queue.addAll(tracks);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isPlaying() {
        AudioTrack ptrack = player.getPlayingTrack();
        if(currentTrack == null && ptrack != null) {
            currentTrack = ptrack;
        }
        return currentTrack != null;
    }

    public boolean joinAudioChannel(DiscordCommandIssuer user) {
        long connected = player.getLink().getChannelId();
        AudioChannel userChannel = user.getVoiceChannel();
        if (connected != -1 && isPlaying()) {
            return true;
        }
        if (userChannel != null) {
            try {
                player.getLink().connect(Objects.requireNonNull(userChannel.getGuild().getVoiceChannelById(userChannel.getIdLong())));
                logger.info("{} => Connecting to {}", userChannel.getGuild().getName(), userChannel.getName());
                return true;
            } catch (InsufficientPermissionException e) {
                user.sendMessage(String.join("\n", "Failed to join your channel because of missing permission `" + e.getPermission() + "`",
                        "This may be due to a discord bug, please ensure the bot is an `Administrator` or add a channel specific override for `VOICE_CONNECT` and `VOICE_MOVE_OTHERS`"));
            }
        }
        return false;
    }

    private void sendEmbed(AudioTrack track, DiscordCommandIssuer sender) {
        sender.sendMessage(AudioEmbedUtils.getNowPlayingEmbed(sender, track));
    }

    public List<AudioTrack> getQueue() {
        List<AudioTrack> tracks;
        lock.lock();
        try {
            tracks = new ArrayList<>(queue);
        } finally {
            lock.unlock();
        }
        return tracks;
    }

    public void skipTo(AudioTrack track) {
        lock.lock();
        try {
            if (!queue.contains(track))
                return;

            while (queue.peek() != track) {
                if (repeatQueue)
                    queue.addLast(queue.pop());
                else
                    queue.pop();
            }
            skip();
        } finally {
            lock.unlock();
        }
    }

    public AudioTrack getLastTrack() {
        if (lastTrack != null) {
            return lastTrack;
        }
        return null;
    }

    public AudioTrack getCurrentTrack() {
        if (currentTrack != null) {
            return currentTrack;
        }
        return null;
    }

    // Events
    private void onTrackStart(TrackStartEvent event) {
        logger.info("{} => Track start {}", player.getManager().getGuild().getName(), event.getTrack().getInfo().getTitle());
        currentTrack = event.getTrack();
    }

    public void onTrackEnd(TrackEndEvent event) {
        logger.info("{} => Track end {} - Reason: {}", player.getManager().getGuild().getName(), event.getTrack().getInfo().getTitle(), event.getReason());
        IPlayer player = event.getPlayer();
        AudioTrackEndReason endReason = event.getReason();
        switch (endReason) {
            case FINISHED:
                if (repeatTrack) {
                    player.playTrack(getCurrentTrack());
                    break;
                }
            case LOAD_FAILED:
                skip();
                break;
            case STOPPED:
                stop();
                break;
        }
    }

    public void onTrackException(TrackExceptionEvent event) {
        AudioTrack track = event.getTrack();
        Exception exception = event.getException();
        AudioManager manager = player.getManager();
        logger.warn(String.format("%s => Track exception for %s", player.getManager().getGuild().getName(), track.getInfo().getTitle()), exception);
        if (exception == null) {
            logger.warn("Null exception in audio manager");
            return;
        }
        if (repeatTrack) {
            repeatTrack = false;
        }
        if (exception.getCause().getMessage().toLowerCase().contains("read timed out")) {
            queue.addFirst(event.getTrack());
            return;
        }
        Collection<ScheduledTask> future = manager.listeners.values();
        future.forEach(ScheduledTask::cancel);
        manager.listeners.clear();
        DiscordCommandIssuer requester = event.getTrack().getUserData(DiscordCommandIssuer.class);
        requester.sendMessage(String.format("Failed to play `%s` because, `%s` %s", track.getInfo().getTitle(), exception.getMessage(), (exception.getCause() != null ? "`" + exception.getCause().getMessage() + "`" : "")));
        skip();
    }

    public void onTrackStuck(TrackStuckEvent event) {
        logger.info("{} => Track stuck {}", player.getManager().getGuild().getName(), event.getTrack().getInfo().getTitle());
        AudioTrack track = event.getTrack();
        DiscordCommandIssuer requester = track.getUserData(DiscordCommandIssuer.class);
        requester.sendMessage("Got stuck while playing `" + track.getInfo().getTitle() + "`. It will be skipped.");
        skip();
    }

    @Override
    public void onEvent(PlayerEvent event) {
        logger.warn(event.getClass().getSimpleName());
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
}