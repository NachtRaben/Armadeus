package com.nachtraben.core.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.core.util.Utils;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

    public static boolean debug = Tohsaka.getInstance().isDebugging();

    private GuildMusicManager manager;

    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack lastTrack;
    private AudioTrack currentTrack;

    private boolean repeatTrack;
    private boolean repeatQueue;

    private boolean channelLock;

    private ScheduledFuture<?> afkCheck;
    private long leave = -1;
    private boolean persist = true;

    private final EqualizerFactory factory = new EqualizerFactory();

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        factory.setGain(32, 12);
        factory.setGain(64, 8);
        factory.setGain(125, 4);
        factory.setGain(250, 0);
        factory.setGain(500, -2);
        factory.setGain(1000, -4);
        factory.setGain(2000, 0);
        factory.setGain(4000, 2);
        factory.setGain(8000, 4);
        factory.setGain(16000, 6);
        this.manager = guildMusicManager;
        manager.getPlayer().setFilterFactory(factory);
        queue = new LinkedBlockingDeque<>();
        afkCheck = Utils.getScheduler().scheduleWithFixedDelay(() -> {
            Guild g = manager.getGuild();
            if (g != null && !persist) {
                VoiceChannel v = g.getAudioManager().getConnectedChannel();
                if (v != null) {
                    if (leave != -1)
                        if (debug)
                            LOGGER.debug("Leaving { " + g.getName() + " } in: " + TimeUtil.fromLong(leave - System.currentTimeMillis(), TimeUtil.FormatType.STRING));
                    if ((v.getMembers().size() < 2 || currentTrack == null) && leave == -1) {
                        leave = TimeUnit.MINUTES.toMillis(2) + System.currentTimeMillis();
                    } else if (v.getMembers().size() > 1 && currentTrack != null && leave != -1) {
                        leave = -1;
                    } else if ((v.getMembers().size() < 2 || currentTrack == null) && leave != -1 && System.currentTimeMillis() > leave) {
                        stop();
                        Utils.getExecutor().execute(() -> {
                            g.getAudioManager().closeAudioConnection();
                        });
                    }
                } else if (leave != -1) {
                    leave = -1;
                }
            }
        }, 0L, 5L, TimeUnit.SECONDS);
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
        manager.getPlayer().playTrack(track);
    }

    public void stop() {
        if (debug)
            LOGGER.debug("Stopped");
        synchronized (queue) {
            repeatTrack = false;
            repeatQueue = false;
            //persist = false;
            manager.getPlayer().setPaused(false);
            queue.clear();
            if (isPlaying()) {
                manager.getPlayer().stopTrack();
                currentTrack.getUserData(GuildCommandSender.class).sendMessage(ChannelTarget.MUSIC, "Queue concluded.");
            }
            channelLock = false;
            currentTrack = null;
        }
    }

    public void skip() {
        synchronized (queue) {
            repeatTrack = false;
            lastTrack = currentTrack;
            if(currentTrack != null && repeatQueue)
                queue.addLast(getCurrentTrack());
            if (queue.isEmpty() && isPlaying()) {
                if (debug)
                    LOGGER.debug("Skipped and stopping.");
                stop();
            } else if (!queue.isEmpty()) {
                if (debug)
                    LOGGER.debug("Skipping");
                play(queue.poll());
            } else {
                if (debug)
                    LOGGER.debug("Wut? " + isPlaying());
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
    private boolean joinVoiceChannel(GuildCommandSender requestor) {
        if (manager.getGuild() != null) {
            AudioManager audioManager = manager.getGuild().getAudioManager();
            VoiceChannel connected = audioManager.getConnectedChannel();
            VoiceChannel reqChannel = requestor.getVoiceChannel();
            if (channelLock && connected != null) {
                return true;
            } else if (!channelLock) {
                if (reqChannel != null) {
                    if (debug)
                        LOGGER.debug("Not locked and requester connected.");
                    try {
                        channelLock = true;
                        try {
                            audioManager.setSelfDeafened(true);
                            audioManager.openAudioConnection(reqChannel);
                        } catch (InsufficientPermissionException e) {
                            requestor.sendMessage(ChannelTarget.MUSIC, "Failed to join `" + reqChannel.getName() + "` because `" + e.getMessage() + "`.");
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        channelLock = false;
                        LOGGER.error("Failed to join " + reqChannel.getName() + ".", e);
                    }
                } else if (connected != null) {
                    if (debug)
                        LOGGER.debug("Not locked and connected.");
                    channelLock = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (debug)
            LOGGER.debug("TrackStartEvent");
        GuildCommandSender requestor = track.getUserData(GuildCommandSender.class);
        if (requestor != null) {
            Guild guild = requestor.getGuild();
            if (guild == null) {
                stop();
                player.destroy();
                return;
            }
            if (requestor.getMember() == null) {
                skip();
                return;
            }
            if (!joinVoiceChannel(requestor)) {
                requestor.sendMessage(ChannelTarget.MUSIC, "Sorry but I was unable to join `" + requestor.getVoiceChannel() + "`.");
                stop();
                return;
            }
            if (!repeatTrack)
                sendEmbed(track, requestor);
        } else {
            skip();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (debug)
            LOGGER.debug("TrackStopEvent");
        if (endReason.mayStartNext && repeatTrack) {
            player.playTrack(getCurrentTrack());
        } else if (endReason.mayStartNext) {
            skip();
        } else {
            if (debug)
                LOGGER.debug("What happened... " + endReason.name());
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // TODO: Prevent requeue if error is unrecoverable.
        if (repeatTrack)
            repeatTrack = false;
        LOGGER.warn(String.valueOf(exception.severity));
        if (exception.getCause().getMessage().toLowerCase().contains("read timed out")) {
            queue.addFirst(track.makeClone());
            return;
        }
        GuildCommandSender requestor = track.getUserData(GuildCommandSender.class);
        requestor.sendMessage(ChannelTarget.MUSIC, String.format("Failed to play `%s` because, `%s`.", track.getInfo().title, exception.getMessage()));
        LOGGER.warn("Something went wrong with lavaplayer.", exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        GuildCommandSender requestor = track.getUserData(GuildCommandSender.class);
        requestor.sendMessage(ChannelTarget.MUSIC, "Got stuck while playing `" + track.getInfo().title + "`. It will be skipped.");
        Radio rad = Radio.getByAddress(track.getInfo().uri);
        if (rad != null)
            setRepeatTrack(false);
        skip();
    }

    private void sendEmbed(AudioTrack track, GuildCommandSender sender) {
        EmbedBuilder builder = Utils.getAudioTrackEmbed(track, sender);
        sender.sendMessage(ChannelTarget.MUSIC, builder.build());
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

    public void destroy() {
        if (afkCheck != null && !afkCheck.isCancelled())
            afkCheck.cancel(true);
    }

}
