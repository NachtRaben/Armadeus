package com.nachtraben.core.audio;

import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.nachtraben.core.utils.TimeUtil;
import com.nachtraben.tohsaka.Tohsaka;
import com.nachtraben.tohsaka.audio.TrackMetaManager;
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
public class TrackScheduler extends AudioEventAdapter implements TrackMetaManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

	private final BlockingQueue<AudioTrack> queue;
	private GuildMusicManager guildMusicManager;
	public AudioTrack lastPlayed;
	public boolean repeat = false;

	private boolean playing = false;
	private boolean queued = false;

	public TrackScheduler(GuildMusicManager manager) {
		this.guildMusicManager = manager;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Standard Operations
	 */
	public void queue(AudioTrack track) {
		if (!guildMusicManager.getPlayer().startTrack(track, true)) {
			queue.offer(track);
		}
	}

	public void play(AudioTrack track) {
		queued = true;
		guildMusicManager.getPlayer().playTrack(track);
	}

	public void stop() {
		if (repeat) repeat = false;
		queue.clear();
		guildMusicManager.getPlayer().stopTrack();
	}

	public void skip() {
		if (repeat)
			repeat = false;
		if (!queue.isEmpty()) {
			play(queue.poll());
		} else {
			guildMusicManager.getPlayer().stopTrack();
		}
	}

	public void skipTo(AudioTrack track) {
		AudioTrackInfo ti1 = track.getInfo();
		AudioTrack t;
		while ((t = queue.poll()) != null) {
			AudioTrackInfo ti2 = t.getInfo();
			if (ti1.title.equals(ti2.title) && ti1.author.equals(ti2.author) && ti1.identifier.equals(ti2.identifier)) {
				guildMusicManager.getPlayer().startTrack(t, false);
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
		return queued || playing || !queue.isEmpty();
	}

	public List<AudioTrack> getQueueList() {
		List<AudioTrack> result = new ArrayList<>();
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

	private boolean joinVoiceChannel(Guild guild, User user) {
		LOGGER.debug("Is playing: " + isPlaying());
		if (!isPlaying()) {
			LOGGER.debug("Not playing anything, joining a channel.");
			VoiceChannel v = getVoiceChannel(guild, user);
			if (v != null) {
				try {
					guild.getAudioManager().openAudioConnection(v);
				} catch (Exception e) {
					LOGGER.debug("Failed to join channel.");
					return false;
				}
			} else {
				LOGGER.debug("No suitable voice channel.");
				return false;
			}
		}
		return true;
	}


		/**
		 * Event Overrides
		 */

		@Override
		public void onTrackStart (AudioPlayer player, AudioTrack track){
			queued = false;
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			if (guild == null) {
				LOGGER.debug("Null guild in audio player, destroying player.");
				stop();
				player.destroy();
				return;
			}
			Object requesterID = getTrackMeta(track).get("requester");
			Object channelID = getTrackMeta(track).get("channel");
			if(requesterID == null) {
				LOGGER.debug("requesterID was null!");
				skip();
				return;
			}
			User user = Tohsaka.getInstance().getUser(requesterID.toString());
			if (user == null) {
				LOGGER.debug("Null user in audio player, skipping track.");
				skip();
				return;
			}
			if (!joinVoiceChannel(guild, user)) {
				LOGGER.debug("Failed to join voice channel, stopping player.");
				stop();
				return;
			}
			playing = true;
			TextChannel channel = channelID != null ? guild.getTextChannelById(channelID.toString()) : null;
			if (!repeat) {
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor("Now Playing: ", track.getInfo().uri, null)
						.setDescription(format("Title: %s\nAuthor: %s\nLength: %s", track.getInfo().title, track.getInfo().author, track.getInfo().isStream ? "Stream" : TimeUtil.millisToString(track.getInfo().length, TimeUtil.FormatType.STRING)))
						.setFooter(format("Requested by %s.", user.getName()), user.getAvatarUrl());
				if (track instanceof YoutubeAudioTrack) {
					builder.setThumbnail(format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
				}
				MessageEmbed message = builder.build();
				MessageUtils.sendMessage(MessageTargetType.MUSIC, channel, message);
			}
		}

		@Override
		public void onTrackEnd (AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
			playing = false;
			if (!endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
				AudioTrack t = track.makeClone();
				getTrackMeta(t).putAll(getTrackMeta(track));
				lastPlayed = t;
			}
			if (repeat) {
				AudioTrack t = track.makeClone();
				getTrackMeta(t).putAll(getTrackMeta(track));
				player.startTrack(t, false);
			} else {
				if (endReason.mayStartNext && !queue.isEmpty()) {
					skip();
				} else if (!isPlaying()) {
					Guild guild = guildMusicManager.getGuildManager().getGuild();
					if (guild == null) {
						LOGGER.debug("onTrackEnd null guild!");
						return;
					}
					TextChannel channel = guild.getTextChannelById(getTrackMeta(track).get("channel").toString());
					if (channel == null) {
						LOGGER.debug("onTrackEnd null channel!");
						return;
					}
					MessageUtils.sendMessage(MessageTargetType.MUSIC, channel, "Queue concluded.");
					TrackMetaManager.data.clear();
				}
			}
			deleteTrackMeta(track);
		}

		@Override
		public void onTrackException (AudioPlayer player, AudioTrack track, FriendlyException exception){
			queued = false;
			playing = false;
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			LOGGER.error(format("Failed to play { %s } in { %s#%s } due to a { %s }.", track.getInfo().title, guild.getName(), guild.getId(), exception.getClass().getSimpleName()), exception);
			deleteTrackMeta(player.getPlayingTrack());
			skip();
		}

		@Override
		public void onTrackStuck (AudioPlayer player, AudioTrack track,long thresholdMs){
			Guild guild = guildMusicManager.getGuildManager().getGuild();
			LOGGER.warn(format("Player for { %s#%s } got stuck playing track { %s }. Skipping the track.", guild.getName(), guild.getId(), track.getInfo().title));
			skip();
		}
	}
