package com.nachtraben.core.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.core.util.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class TrackScheduler extends AudioEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

    private GuildMusicManager manager;

    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack lastTrack;
    private AudioTrack currentTrack;

    private boolean repeatTrack;
    private boolean repeatQueue;

    private VoiceChannel currentChannel;

    private ScheduledFuture<?> afkCheck;
    private long leave = -1;

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.manager = guildMusicManager;
        queue = new LinkedBlockingDeque<>();
        afkCheck = Utils.getScheduler().scheduleWithFixedDelay(() -> {
            Guild g = manager.getGuild();
            if(g != null) {
                VoiceChannel v = g.getAudioManager().getConnectedChannel();
                if(v != null) {
                    if(leave != -1)
                        LOGGER.debug("Leaving { " + g.getName() + " } in: " + TimeUtil.millisToString(leave - System.currentTimeMillis(), TimeUtil.FormatType.STRING));
                    if((v.getMembers().size() < 2 || currentTrack == null) && leave == -1) {
                        leave = TimeUnit.MINUTES.toMillis(2) + System.currentTimeMillis();
                    } else if(v.getMembers().size() > 1 && currentTrack != null && leave != -1) {
                        leave = -1;
                    } else if((v.getMembers().size() < 2 || currentTrack == null) && leave != -1 && System.currentTimeMillis() > leave) {
                        stop();
                        g.getAudioManager().closeAudioConnection();
                    }
                } else if(leave != -1) {
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
        synchronized (queue) {
            repeatTrack = false;
            repeatQueue = false;
            manager.getPlayer().setPaused(false);
            manager.getPlayer().stopTrack();
            queue.clear();
            if (isPlaying()) {
                currentTrack.getUserData(GuildCommandSender.class).sendMessage(ChannelTarget.MUSIC, "Queue concluded.");
                currentChannel = null;
                currentTrack = null;
            }
        }
    }

    public void skip() {
        synchronized (queue) {
            repeatTrack = false;
            lastTrack = currentTrack;
            if (queue.isEmpty() && isPlaying()) {
                stop();
            } else if (!queue.isEmpty())
                play(queue.poll());
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
        return currentTrack != null && currentChannel != null;
    }

    private boolean joinVoiceChannel(GuildCommandSender requestor) {
        if (manager.getGuild() != null) {
            AudioManager audioManager = manager.getGuild().getAudioManager();
            if (currentChannel != null && (audioManager.isAttemptingToConnect() || audioManager.isConnected())) {
                currentChannel = audioManager.getConnectedChannel();
                return true;
            } else {
                VoiceChannel channel = requestor.getVoiceChannel();
                if (channel != null) {
                    try {
                        audioManager.openAudioConnection(channel);
                        this.currentChannel = channel;
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
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
        if (endReason.mayStartNext && repeatTrack) {
            player.playTrack(getCurrentTrack());
        } else if (endReason.mayStartNext) {
            if (repeatQueue)
                queue.addLast(getCurrentTrack());
            skip();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // TODO: Prevent requeue if error is unrecoverable.
        if(repeatTrack)
            repeatTrack = false;
        GuildCommandSender requestor = track.getUserData(GuildCommandSender.class);
        requestor.sendMessage(ChannelTarget.MUSIC, String.format("Failed to play `%s` because, `%s`.", track.getInfo().title, exception.getMessage()));
        LOGGER.error("Something went wrong with lavaplayer.", exception);
    }

    private void sendEmbed(AudioTrack track, GuildCommandSender sender) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Now Playing: ",
                EmbedBuilder.URL_PATTERN.matcher(track.getInfo().uri).matches()
                        ? track.getInfo().uri : null, null)
                .setColor(Utils.randomColor())
                .setFooter("Requested by: " + sender.getMember().getEffectiveName(), sender.getUser().getAvatarUrl())
                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
                        track.getInfo().title,
                        track.getInfo().author,
                        track.getInfo().isStream ? "Stream" : TimeUtil.millisToString(track.getInfo().length, TimeUtil.FormatType.STRING)));
        if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
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

    public void destroy() {
        if(afkCheck != null && !afkCheck.isCancelled())
            afkCheck.cancel(true);
    }

}
