package com.nachtraben.core.audio;

import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.nachtraben.core.utils.TimeUtil;
import com.nachtraben.tohsaka.TrackContext;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.nachtraben.core.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/16/2017.
 */
public class TrackScheduler extends AudioEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

	private GuildMusicManager guildMusicManager;

	private final BlockingQueue<TrackContext> queue;

	public TrackContext lastTrack;
	public TrackContext currentTrack;

	public boolean repeat = false;

	public TrackScheduler(GuildMusicManager manager) {
		this.guildMusicManager = manager;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Standard Operations
	 */
	public void queue(TrackContext track) {
		queue.offer(track);
		if(!isPlaying()) skip();
	}

	public void play(TrackContext track) {
		currentTrack = track;
		guildMusicManager.getPlayer().playTrack(track.getTrack());
	}

	public void stop() {
		if (repeat) repeat = false;
		guildMusicManager.getPlayer().setPaused(false);
		queue.clear();
		guildMusicManager.getPlayer().stopTrack();
		MessageUtils.sendMessage(MessageTargetType.MUSIC, currentTrack.getTextChannel(), "Queue concluded.");
		currentTrack = null;
	}

	public void skip() {
		LOGGER.debug("Skipping with " + queue.size() + " songs in queue.");
		if(repeat) repeat = false;
		if(queue.isEmpty() /*&& (currentTrack.getTrack().getState().equals(AudioTrackState.INACTIVE) || currentTrack.getTrack().getState().equals(AudioTrackState.FINISHED))*/) {
			LOGGER.debug("Queue was empty, stopping.");
			stop();
		} else {
			play(queue.poll());
		}
	}

	public void skipTo(TrackContext track) {
		AudioTrackInfo ti1 = track.getTrack().getInfo();
		TrackContext t;
		while ((t = queue.poll()) != null) {
			AudioTrackInfo ti2 = t.getTrack().getInfo();
			if (ti1.title.equals(ti2.title) && ti1.author.equals(ti2.author) && ti1.identifier.equals(ti2.identifier)) {
				play(t);
				return;
			}
		}
	}

	public void shuffle() {
		List<TrackContext> tracks = new ArrayList<>();
		queue.drainTo(tracks);
		Collections.shuffle(tracks);
		queue.addAll(tracks);
	}

	/**
	 * Utilities
	 */

	public boolean isPlaying() {
		return currentTrack != null;
	}

	public List<TrackContext> getQueueList() {
		List<TrackContext> result = new ArrayList<>();
		result.addAll(queue);
		return result;
	}

	private VoiceChannel getVoiceChannel(Guild guild, User user) {
		Member m = guild.getMember(user);
		if (m != null && m.getVoiceState().inVoiceChannel()) {
			return m.getVoiceState().getChannel();
		} else if (!guild.getVoiceChannels().isEmpty()) {
			return guild.getVoiceChannels().get(0);
		}
		return null;
	}

	private boolean joinVoiceChannel() {
		Guild guild = guildMusicManager.getGuildManager().getGuild();
		VoiceChannel v = getVoiceChannel(guild, currentTrack.getRequester());
		if (v != null) {
			try {
				guild.getAudioManager().openAudioConnection(v);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}


		/**
		 * Event Overrides
		 */

		@Override
		public void onTrackStart (AudioPlayer player, AudioTrack track){
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			if (guild == null) {
				LOGGER.debug("Null guild in audio player, destroying player.");
				stop();
				player.destroy();
				return;
			}
			if(currentTrack.getRequester() == null) {
				LOGGER.debug("Requester was null! Skipping track.");
				skip();
				return;
			}
			if (!joinVoiceChannel()) {
				LOGGER.debug("Failed to join voice channel, stopping player.");
				stop();
				return;
			}
			if (!repeat) {
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor("Now Playing: ", track.getInfo().uri, null)
						.setDescription(format("Title: %s\nAuthor: %s\nLength: %s", track.getInfo().title, track.getInfo().author, track.getInfo().isStream ? "Stream" : TimeUtil.millisToString(track.getInfo().length, TimeUtil.FormatType.STRING)))
						.setFooter(format("Requested by %s.", guild.getMember(currentTrack.getRequester()).getEffectiveName()), currentTrack.getRequester().getAvatarUrl());
				if (track instanceof YoutubeAudioTrack) {
					builder.setThumbnail(format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
				}
				MessageEmbed message = builder.build();
				MessageUtils.sendMessage(MessageTargetType.MUSIC, currentTrack.getTextChannel(), message);
			}
		}

		@Override
		public void onTrackEnd (AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
				if (endReason.mayStartNext) {
					if(repeat)
						play(currentTrack.clone());
					else
						skip();
				}
		}

		@Override
		public void onTrackException (AudioPlayer player, AudioTrack track, FriendlyException exception){
			currentTrack = null;
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			MessageUtils.sendMessage(MessageTargetType.MUSIC, currentTrack.getTextChannel(), format("Failed to play { %s } because of an exception, %s", currentTrack.getTrack().getInfo().title, exception.getMessage()));
			LOGGER.error(format("Failed to play { %s } in { %s#%s } due to a { %s }.", track.getInfo().title, guild.getName(), guild.getId(), exception.getClass().getSimpleName()), exception);
			skip();
		}

		@Override
		public void onTrackStuck (AudioPlayer player, AudioTrack track,long thresholdMs){
			currentTrack = null;
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			LOGGER.warn(format("Player for { %s#%s } got stuck playing track { %s }. Skipping the track.", guild.getName(), guild.getId(), track.getInfo().title));
			skip();
		}
	}
