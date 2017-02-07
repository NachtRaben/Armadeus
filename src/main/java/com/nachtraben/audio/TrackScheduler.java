package com.nachtraben.audio;

import com.nachtraben.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.xilixir.fw.utils.LogManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.xilixir.fw.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/16/2017.
 */
public class TrackScheduler extends AudioEventAdapter implements TrackMetaManager {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    public Guild guild;

    public boolean repeat = false;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void shuffle() {
        List<AudioTrack> tracks = getQueueList();
        queue.clear();
        Collections.shuffle(tracks);
        queue.addAll(tracks);
    }

    public void stop() {
        if(repeat) repeat = false;
        queue.clear();
        player.stopTrack();
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        if(repeat) repeat = false;
        player.startTrack(queue.poll(), false);
    }

    public void skipTo(AudioTrack track) {
        AudioTrackInfo ti1 = track.getInfo();
        AudioTrack t;
        while ((t = queue.poll()) != null) {
            AudioTrackInfo ti2 = t.getInfo();
            if (ti1.title.equals(ti2.title) && ti1.author.equals(ti2.author) && ti1.identifier.equals(ti2.identifier)) {
                player.startTrack(t, false);
                return;
            }
        }
    }

    public List<AudioTrack> getQueueList() {
        List<AudioTrack> result = new ArrayList<>();
        result.addAll(queue);
        return result;
    }

    /**
     * Event Overrides
     */

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(repeat) {
            AudioTrack t = track.makeClone();
            getTrackMeta(t).putAll(getTrackMeta(track));
            player.startTrack(t, false);
        } else {
            if (endReason.mayStartNext && !queue.isEmpty()) {
                nextTrack();
            } else if (queue.isEmpty()) {
                TextChannel channel = guild.getJDA().getTextChannelById(getTrackMeta(track).get("tchan").toString());
                if(!guild.getId().equals("205076586834165760") && !channel.getId().equals("205076586834165760"))
                    channel.sendMessage("Queue concluded.").queue();
                data.clear();
                //if (guild.getAudioManager().isConnected()) guild.getAudioManager().closeAudioConnection();
            }
        }
        deleteTrackMeta(track);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        User user = guild.getJDA().getUserById(getTrackMeta(track).get("requester").toString());
        if(user == null) {
            LogManager.TOHSAKA.error(format("Attempted to play track in { %s#%s } with a null user as its requester!", guild.getName(), guild.getId()));
            nextTrack();
        } else {
            if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) {
                VoiceChannel v = getVoiceChannel(user);
                if(v != null) {
                    LogManager.TOHSAKA.info(format("No current voice connection, connecting to %s.", v.getName()));
                    guild.getAudioManager().openAudioConnection(v);
                }
                else {
                    LogManager.TOHSAKA.info("Failed to find a suitable channel to join, aborting queue.");
                    stop();
                }
            }
            TextChannel channel = guild.getJDA().getTextChannelById(getTrackMeta(track).get("tchan").toString());
            if(channel == null) {
                LogManager.TOHSAKA.error(format("Attempted to send now playing message to a null text channel in { %s#%s }.", guild.getName(), guild.getId()));
            } else {
                if(!repeat) {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setAuthor("Now Playing: ",null, null)
                            .setDescription(format("Title: %s\nAuthor: %s\nLength: %s", track.getInfo().title, track.getInfo().author, track.getInfo().isStream ? "Stream" : TimeUtil.millisToString(track.getInfo().length, TimeUtil.FormatType.STRING)))
                            .setFooter(format("Requested by %s.", user.getName()), user.getAvatarUrl());
                    if(track instanceof YoutubeAudioTrack) {
                        builder.setAuthor("Now Playing: ", format("https://www.youtube.com/watch?v=%s", track.getIdentifier()), null);
                        builder.setThumbnail(format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
                    }
                    MessageEmbed message = builder.build();
                    if(!guild.getId().equals("205076586834165760") && !channel.getId().equals("205076586834165760"))
                        channel.sendMessage(message).queue();
                }
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        LogManager.TOHSAKA.error(format("Failed to play { %s } in { %s#%s } due to a { %s }.", track.getInfo().title, guild.getName(), guild.getId(), exception.getClass().getSimpleName()), exception);
        deleteTrackMeta(player.getPlayingTrack());
        nextTrack();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        LogManager.TOHSAKA.warn(format("Player for { %s#%s } got stuck playing track { %s }. Skipping the track.", guild.getName(), guild.getId(), track.getInfo().title));
        nextTrack();
    }

    private VoiceChannel getVoiceChannel(User user) {
        Member m = guild.getMember(user);
        if(m != null && m.getVoiceState().inVoiceChannel()) {
            return m.getVoiceState().getChannel();
        } else if(!guild.getVoiceChannels().isEmpty()){
            guild.getVoiceChannels().get(0);
        }
        return null;
    }

}
