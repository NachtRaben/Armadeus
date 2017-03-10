package com.nachtraben.tohsaka.audio;

import com.nachtraben.core.utils.LogManager;
import com.nachtraben.core.utils.TimeUtil;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.nachtraben.core.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/16/2017.
 */
public class TrackScheduler extends AudioEventAdapter implements TrackMetaManager {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    private final String guildID;
    public AudioTrack lastPlayed;
    public boolean repeat = false;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.guildID = guild.getId();
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     *  Standard Operations
     */

    public void queue(AudioTrack track) {
    	System.out.println("QUEUETRACKHASH: " + hashCode());
        System.out.println("BEFORE: " + queue.size());
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
        System.out.println("AFTER: " + queue.size());
    }

    public void play(AudioTrack track) {
        player.playTrack(track);
    }

    public void stop() {
        if(repeat) repeat = false;
        queue.clear();
        player.stopTrack();
    }

    public void skip() {
        if(repeat)
        	repeat = false;
        if(!queue.isEmpty()) {
            System.out.println("Queue wasn't empty");
            play(queue.poll());
        } else {
        	System.out.println("Queue was empty!");
		}
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

    public void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>();
        queue.drainTo(tracks);
        Collections.shuffle(tracks);
        queue.addAll(tracks);
    }

    /**
     * Utilities
     */

    public boolean isPlaying() {
        return player.getPlayingTrack() != null || !queue.isEmpty();
    }

    public List<AudioTrack> getQueueList() {
        List<AudioTrack> result = new ArrayList<>();
        result.addAll(queue);
        return result;
    }

    private VoiceChannel getVoiceChannel(Guild guild, User user) {
        Member m = guild.getMember(user);
        if(m != null && m.getVoiceState().inVoiceChannel()) {
            return m.getVoiceState().getChannel();
        } else if(!guild.getVoiceChannels().isEmpty()){
            guild.getVoiceChannels().get(0);
        }
        return null;
    }

    private boolean joinVoiceChannel(Guild guild, User user) {
        if(queue.isEmpty()) {
            System.out.println("Not playing anything, joining a channel.");
            VoiceChannel v = getVoiceChannel(guild, user);
            if(v != null) {
                try {
                    guild.getAudioManager().openAudioConnection(v);
                } catch(Exception e) {
                    System.out.println("Failed to join channel, stopping.");
                    stop();
                    return false;
                }
            } else {
                System.out.println("No suitable voice channel, stopping.");
                stop();
                return false;
            }
        }
        return true;
    }


    /**
     * Event Overrides
     */

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Guild guild = Tohsaka.getInstance().getGuildById(guildID);
        if(guild == null) {
            System.out.println("Null guild in audio player, destroying player.");
            stop();
            player.destroy();
            return;
        }
        User user = Tohsaka.getInstance().getUser(getTrackMeta(track).get("requester").toString());
        if(user == null) {
            System.out.println("Null user in audio player, skipping track.");
            skip();
            return;
        }
        if(!joinVoiceChannel(guild, user)) {
            System.out.println("Failed to join voice channel, skipping track.");
            skip();
            return;
        }
        TextChannel channel = guild.getTextChannelById(getTrackMeta(track).get("tchan").toString());
        if(channel != null) {
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
                channel.sendMessage(message).queue();
            }
        } else {
            LogManager.TOHSAKA.error(format("Attempted to send now playing message to a null text channel in { %s#%s }.", guild.getName(), guild.getId()));
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
            AudioTrack t = track.makeClone();
            getTrackMeta(t).putAll(getTrackMeta(track));
            lastPlayed = t;
        }
        if(repeat) {
            AudioTrack t = track.makeClone();
            getTrackMeta(t).putAll(getTrackMeta(track));
            player.startTrack(t, false);
        } else {
            if (endReason.mayStartNext && !queue.isEmpty()) {
                skip();
            } else if (!isPlaying()) {
//                Guild guild = Tohsaka.getInstance().getGuildById(guildID);
//                if(guild == null) {
//                    System.out.println("onTrackEnd null guild!");
//                    return;
//                }
//                TextChannel channel = guild.getTextChannelById(getTrackMeta(track).get("tchan").toString());
//                if(channel == null) {
//                    System.out.println("onTrackEnd null channel!");
//                    return;
//                }
//                channel.sendMessage("Queue concluded.").queue();
//                TrackMetaManager.data.clear();
            }
        }
        deleteTrackMeta(track);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Guild guild = Tohsaka.getInstance().getGuildById(guildID);
        LogManager.TOHSAKA.error(format("Failed to play { %s } in { %s#%s } due to a { %s }.", track.getInfo().title, guild.getName(), guild.getId(), exception.getClass().getSimpleName()), exception);
        deleteTrackMeta(player.getPlayingTrack());
        skip();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        Guild guild = Tohsaka.getInstance().getGuildById(guildID);
        LogManager.TOHSAKA.warn(format("Player for { %s#%s } got stuck playing track { %s }. Skipping the track.", guild.getName(), guild.getId(), track.getInfo().title));
        skip();
    }

}
