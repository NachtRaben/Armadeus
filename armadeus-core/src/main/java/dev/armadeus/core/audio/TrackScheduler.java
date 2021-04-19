package dev.armadeus.core.audio;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.EmbedUtils;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler extends AudioEventAdapter implements IPlayerEventListener {

    private static final Logger logger = LogManager.getLogger();

    public static boolean debug = DiscordBot.get().isDevMode();

    private final GuildMusicManager manager;

    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack lastTrack;
    private AudioTrack currentTrack;

    private boolean repeatTrack;
    private boolean repeatQueue;

    private boolean channelLock;

    private boolean persist = true;

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.manager = guildMusicManager;
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
        currentTrack = track;
        if (debug)
            logger.debug("TrackStartEvent");
        DiscordUser requester = track.getUserData(DiscordUser.class);
        if (requester != null) {
            Guild guild = requester.getGuild();
            if (guild == null) {
                stop();
                return;
            }
            if (requester.getMember() == null) {
                skip();
                return;
            }
            if (!joinVoiceChannel(requester)) {
                requester.sendMessage("Sorry but I was unable to join `" + requester.getVoiceChannel() + "`.");
                stop();
                return;
            }
            if (!repeatTrack)
                sendEmbed(track, requester);
        } else {
            skip();
        }
        manager.getPlayer().playTrack(track);
    }

    public void stop() {
        if (debug)
            logger.debug("Stopped");
        synchronized (queue) {
            repeatTrack = false;
            repeatQueue = false;
            //persist = false;
            manager.getPlayer().setPaused(false);
            queue.clear();
            if (isPlaying()) {
                manager.getPlayer().stopTrack();
                currentTrack.getUserData(DiscordUser.class).sendMessage("Queue concluded.");
            }
            channelLock = false;
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
                if (debug)
                    logger.debug("Skipped and stopping.");
                stop();
            } else if (!queue.isEmpty()) {
                if (debug)
                    logger.debug("Skipping");
                play(queue.poll());
            } else {
                if (debug)
                    logger.debug("Wut? " + isPlaying());
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
        return currentTrack != null && channelLock;
    }

    // TODO: Rewrite voice channel logic.
    // TODO: If requester channel is null but I'm in a channel.
    private boolean joinVoiceChannel(DiscordUser requester) {
        if (manager.getGuild() != null) {
            String connected = manager.getLink().getChannel();
            if (requester.getGuild().getAudioManager().isConnected() && connected != null) {
                logger.error("Found a stray voice connection, attempting to close!");
                requester.getGuild().getAudioManager().closeAudioConnection();
            }
            VoiceChannel reqChannel = requester.getVoiceChannel();
            if (channelLock && connected != null) {
                return true;
            } else if (!channelLock) {
                if (reqChannel != null) {
                    if (debug)
                        logger.debug("Not locked and requester connected.");
                    try {
                        channelLock = true;
                        try {
                            manager.getLink().connect(reqChannel);
                        } catch (InsufficientPermissionException e) {
                            requester.sendMessage("Failed to join `" + reqChannel.getName() + "` because `" + e.getMessage() + "`.");
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        channelLock = false;
                        logger.error("Failed to join " + reqChannel.getName() + ".", e);
                    }
                } else if (connected != null) {
                    if (debug)
                        logger.debug("Not locked and connected.");
                    channelLock = true;
                    return true;
                }
            }
        }
        return false;
    }

    public void onTrackStart(TrackStartEvent event) {

    }

    public void onTrackEnd(TrackEndEvent event) {
        IPlayer player = event.getPlayer();
        AudioTrackEndReason endReason = event.getReason();
        if (debug)
            logger.debug("TrackStopEvent");
        if (endReason.mayStartNext && repeatTrack) {
            player.playTrack(getCurrentTrack());
        } else if (endReason.mayStartNext) {
            skip();
        } else {
            if (debug)
                logger.debug("What happened... " + endReason.name());
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
        Collection<Future<?>> future = manager.listeners.values();
        future.forEach(f -> f.cancel(true));
        manager.listeners.clear();
        DiscordUser requester = event.getTrack().getUserData(DiscordUser.class);
        requester.sendMessage(String.format("Failed to play `%s` because, `%s`", track.getInfo().title, exception.getMessage()));
        logger.warn("Something went wrong with lavaplayer.", exception);

    }

    public void onTrackStuck(TrackStuckEvent event) {
        AudioTrack track = event.getTrack();
        DiscordUser requestor = track.getUserData(DiscordUser.class);
        requestor.sendMessage("Got stuck while playing `" + track.getInfo().title + "`. It will be skipped.");
        Radio rad = Radio.getByAddress(track.getInfo().uri);
        if (rad != null)
            setRepeatTrack(false);
        skip();
    }

    private void sendEmbed(AudioTrack track, DiscordUser sender) {
        EmbedBuilder builder = EmbedUtils.getNowPlayingEmbed(sender, track);
        sender.sendMessage(builder.build());
    }

    public List<AudioTrack> getQueue() {
        List<AudioTrack> tracks = new ArrayList<>();
        synchronized (queue) {
            tracks.addAll(queue);
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

    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean b) {
        this.persist = b;
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
}