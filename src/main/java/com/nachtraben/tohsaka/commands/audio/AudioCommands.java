package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.audio.GuildMusicManager;
import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.HasteBin;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.nachtraben.core.utils.TimeUtil;
import com.nachtraben.tohsaka.TrackContext;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.nachtraben.core.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AudioCommands {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioCommands.class);

    @Cmd(name = "play", format = "{target}", description = "Playes the following URL or searches youtube.", flags = { "-pr", "--randomize", "--random", "--playlist"})
    public void play(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			String trackUrl = args.get("target");
			LOGGER.debug("Attempting to play track: " + trackUrl);
			boolean randomize = flags.containsKey("r") || flags.containsKey("random") || flags.containsKey("randomize");
			boolean playlist = flags.containsKey("p") || flags.containsKey("playlist");
			loadAndPlay(sendee, trackUrl, randomize, playlist);
		}
    }

    @Cmd(name = "last", format = "", description = "Plays the last successful song.")
    public void playLast(CommandSender sender) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			TrackScheduler scheduler = getGuildAudioPlayer(sendee.getMessage().getGuild()).getScheduler();
			if(scheduler.lastTrack != null) {
				scheduler.queue(scheduler.lastTrack.clone());
			}
		}
    }

    @Cmd(name = "pause", format = "", description = "Toggles the pause state of the bot.")
    public void pause(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			musicManager.getPlayer().setPaused(!musicManager.getPlayer().isPaused());
			MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Player is now, " + (musicManager.getPlayer().isPaused() ? "paused." : "un-paused:"));
		}
    }

    @Cmd(name = "stop", format = "", description = "Stops audio playback of the bot.")
    public void stop(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			TrackScheduler scheduler = getGuildAudioPlayer(sendee.getMessage().getGuild()).getScheduler();
			scheduler.stop();
		}
    }

    @Cmd(name = "vol", format = "<volume>", description = "Changes the volume of the bot.")
    public void vol(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getGuild());
			Integer i;
			try {
				i = Integer.parseInt(args.get("volume"));
			} catch (NumberFormatException e) {
				return;
			}
			musicManager.getPlayer().setVolume(i);
			MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Volume set to " + i + "/150.");
		}
    }

    @Cmd(name = "skip", format = "", description = "Skips to the next song in the queue.")
    public void skip(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			getGuildAudioPlayer(sendee.getGuild()).getScheduler().skip();
		}
    }

    @Cmd(name = "buffer", format = "<time>", description = "Skips to the desired time in the currently playing track.")
    public void buffer(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			if (musicManager.getPlayer().getPlayingTrack().isSeekable()) {
				long time = TimeUtil.stringToMillis(args.get("time"));
				if (time > musicManager.getPlayer().getPlayingTrack().getDuration()) {
					MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "You cannot buffer past " + TimeUtil.millisToString(musicManager.getPlayer().getPlayingTrack().getDuration(), TimeUtil.FormatType.STRING) + " for this track.");
					return;
				}
				MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Buffering to " + args.get("time") + ".");
				musicManager.getPlayer().getPlayingTrack().setPosition(time);
			} else {
				MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "You cannot buffer a streamed track.");
			}
		}
    }

    @Cmd(name = "queue", format = "", description = "Shows the current song queue in hastebin format.")
    public void queuelist(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			StringBuilder sb = new StringBuilder();
			if (man.getPlayer().getPlayingTrack() != null) {
				AudioTrackInfo info = man.getPlayer().getPlayingTrack().getInfo();
				sb.append("Current) ").append(info.title).append(" by ").append(info.author).append(" for ").append(TimeUtil.millisToString(info.length, TimeUtil.FormatType.STRING)).append(".\n");
			}
			List<TrackContext> queue = man.getScheduler().getQueueList();
			for (int i = 0; i < queue.size(); i++) {
				AudioTrack audioTrack = queue.get(i).getTrack();
				sb.append(i).append(") ").append(audioTrack.getInfo().title).append(" by ").append(audioTrack.getInfo().author).append(" for ").append(TimeUtil.millisToString(audioTrack.getInfo().length, TimeUtil.FormatType.STRING)).append(".\n");
			}
			HasteBin hastebin = new HasteBin(sb.toString());
			MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Here's the queue for ya! " + hastebin.getHaste());
		}
    }

    @Cmd(name = "skipto", format = "{rest}", description = "Skips to the desired index or song in the queue.")
    public void queueto(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			boolean isIndex = false;
			int index = -1;
			String identifier = args.get("rest");
			if (identifier == null) return;
			try {
				index = Integer.parseInt(identifier);
				isIndex = true;
			} catch (Exception ignored) {
			}
			TrackScheduler scheduler = getGuildAudioPlayer(sendee.getMessage().getGuild()).getScheduler();

			List<TrackContext> queue = scheduler.getQueueList();
			if (isIndex && index > queue.size()) return;
			for (int i = 0; i < queue.size(); i++) {
				TrackContext track = queue.get(i);
				if (isIndex && i == index) {
					MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), format("Skipping to queue index `%s`.", i));
					scheduler.skipTo(track);
					return;
				} else if (!isIndex && track.getTrack().getInfo().title.toLowerCase().contains(identifier.toLowerCase())) {
					MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), format("Skipping to queue index `%s`.", i));
					scheduler.skipTo(track);
					return;
				}
			}
			MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), format("`%s` wasn't found anywhere in the queue!", identifier));
		}
    }

    @Cmd(name = "repeat", format = "", description = "Toggles the repeat status of the current song.")
    public void repeat(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			boolean repeat = !man.getScheduler().repeat;
			man.getScheduler().repeat = repeat;
			MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), format("Repeating: %s", repeat));
		}
    }

    @Cmd(name = "shuffle", format = "", description = "Shuffles the song queue.")
    public void shuffle(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager manager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			manager.getScheduler().shuffle();
		}
    }

    private void loadAndPlay(final GuildCommandSender sender, final String trackUrl, boolean shuffle, boolean preserveplaylist) {
        GuildMusicManager musicManager = getGuildAudioPlayer(sender.getGuild());
        GuildMusicManager.DEFAULT_PLAYER_MANAGER.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                MessageUtils.sendMessage(MessageTargetType.MUSIC, sender.getChannel(), String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
                musicManager.getScheduler().queue(new TrackContext(track, sender.getUser(), sender.getChannel()));
			}

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult() && !preserveplaylist) {
					trackLoaded(playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0));
                } else {
                    if (shuffle) Collections.shuffle(playlist.getTracks());
                    playlist.getTracks().forEach(audioTrack -> musicManager.getScheduler().queue(new TrackContext(audioTrack, sender.getUser(), sender.getChannel())));
                    MessageUtils.sendMessage(MessageTargetType.MUSIC, sender.getChannel(), String.format("Added `%s` tracks to the queue for you from `%s`. :3 %s", playlist.getTracks().size(), playlist.getName(), shuffle ? "randomized!" : ""));
                }
            }

            @Override
            public void noMatches() {
                if (trackUrl.startsWith("ytsearch:"))
                    MessageUtils.sendMessage(MessageTargetType.MUSIC, sender.getChannel(), "Nothing found by `" + trackUrl.replace("ytsearch: ", "") + "`.");
                else
                    loadAndPlay(sender, "ytsearch:" + trackUrl, shuffle, preserveplaylist);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MessageUtils.sendMessage(MessageTargetType.MUSIC, sender.getChannel(), format("Sorry, failed to load that track because of { %s }", exception.getMessage()));
                LOGGER.error(format("Failed to load track in guild { %s#%s } due to a { %s }.", sender.getGuild().getName(), sender.getGuild().getId(), exception.getClass()), exception);
            }
        });
	}

    private GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildManager manager = GuildManager.getManagerFor(guild.getId());
        return manager.getAudioManager();
    }

}
