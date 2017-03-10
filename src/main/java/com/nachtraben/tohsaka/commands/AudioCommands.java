package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.GuildManager;
import com.nachtraben.core.audio.GuildMusicManager;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Cmd;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.utils.HasteBin;
import com.nachtraben.core.utils.LogManager;
import com.nachtraben.core.utils.TimeUtil;
import com.nachtraben.tohsaka.audio.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.nachtraben.core.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AudioCommands {

    @Cmd(name = "play", format = "{target}", description = "Playes the following URL or searches youtube.", flags = { "-pr", "--randomize", "--random", "--playlist"})
    public void play(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			String trackUrl = args.get("target");
			boolean randomize = flags.containsKey("r") || flags.containsKey("random") || flags.containsKey("randomize");
			boolean playlist = flags.containsKey("p") || flags.containsKey("playlist");
			loadAndPlay(sendee.getMessage(), trackUrl, randomize, playlist);
		}
    }

    @Cmd(name = "last", format = "", description = "Plays the last successful song.")
    public void playLast(CommandSender sender) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) musicManager.getScheduler();
			if(scheduler.lastPlayed != null) {
				scheduler.queue(scheduler.lastPlayed);
			}
		}
    }

    @Cmd(name = "pause", format = "", description = "Toggles the pause state of the bot.")
    public void pause(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			musicManager.getPlayer().setPaused(!musicManager.getPlayer().isPaused());
		}
    }

    @Cmd(name = "stop", format = "", description = "Stops audio playback of the bot.")
    public void stop(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) musicManager.getScheduler();
			scheduler.stop();
		}
    }

    @Cmd(name = "vol", format = "<volume>", description = "Changes the volume of the bot.")
    public void vol(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getGuild());
			Integer i = null;
			try {
				i = Integer.parseInt(args.get("volume"));
			} catch (NumberFormatException e) {
				return;
			}
			musicManager.getPlayer().setVolume(i);
			sendee.getChannel().sendMessage("Volume set to " + i + "/150.").queue();
		}
    }

    @Cmd(name = "skip", format = "", description = "Skips to the next song in the queue.")
    public void skip(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager musicManager = getGuildAudioPlayer(sendee.getGuild());
			TrackScheduler scheduler = (TrackScheduler) musicManager.getScheduler();
			System.out.println("SKIPHASH: " + scheduler.hashCode());
			scheduler.skip();
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
					sendee.getMessage().getTextChannel().sendMessage("You cannot buffer past " + TimeUtil.millisToString(musicManager.getPlayer().getPlayingTrack().getDuration(), TimeUtil.FormatType.STRING) + " for this track.").queue();
					return;
				}
				sendee.getMessage().getTextChannel().sendMessage("Buffering to " + args.get("time") + ".").queue();
				musicManager.getPlayer().getPlayingTrack().setPosition(time);
			} else {
				sendee.getMessage().getTextChannel().sendMessage("You cannot buffer a streamed track.").queue();
			}
		}
    }

    @Cmd(name = "queue", format = "", description = "Shows the current song queue in hastebin format.")
    public void queuelist(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) man.getScheduler();
			StringBuilder sb = new StringBuilder();
			if (man.getPlayer().getPlayingTrack() != null) {
				AudioTrackInfo info = man.getPlayer().getPlayingTrack().getInfo();
				sb.append("Current) ").append(info.title).append(" by ").append(info.author).append(" for ").append(TimeUtil.millisToString(info.length, TimeUtil.FormatType.STRING)).append(".\n");
			}
			List<AudioTrack> queue = scheduler.getQueueList();
			for (int i = 0; i < queue.size(); i++) {
				AudioTrack audioTrack = queue.get(i);
				sb.append(i).append(") ").append(audioTrack.getInfo().title).append(" by ").append(audioTrack.getInfo().author).append(" for ").append(TimeUtil.millisToString(audioTrack.getInfo().length, TimeUtil.FormatType.STRING)).append(".\n");
			}
			HasteBin hastebin = new HasteBin(sb.toString());
			sendee.getMessage().getTextChannel().sendMessage("Here's the queue for ya! " + hastebin.getHaste()).queue();
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
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) man.getScheduler();

			List<AudioTrack> queue = scheduler.getQueueList();
			if (isIndex && index > queue.size()) return;
			for (int i = 0; i < queue.size(); i++) {
				AudioTrack track = queue.get(i);
				if (isIndex && i == index) {
					sendee.getMessage().getTextChannel().sendMessage(format("Skipping to queue index `%s`.", i)).queue();
					scheduler.skipTo(track);
					return;
				} else if (!isIndex && track.getInfo().title.toLowerCase().contains(identifier.toLowerCase())) {
					sendee.getMessage().getTextChannel().sendMessage(format("Skipping to queue index `%s`.", i)).queue();
					scheduler.skipTo(track);
					return;
				}
			}
			sendee.getMessage().getTextChannel().sendMessage(format("`%s` wasn't found anywhere in the queue!", identifier)).queue();
		}
    }

    @Cmd(name = "repeat", format = "", description = "Toggles the repeat status of the current song.")
    public void repeat(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) man.getScheduler();
			boolean repeat = !scheduler.repeat;
			scheduler.repeat = repeat;
			sendee.getMessage().getTextChannel().sendMessage(format("Repeating: %s", repeat)).queue();
		}
    }

    @Cmd(name = "shuffle", format = "", description = "Shuffles the song queue.")
    public void shuffle(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			GuildMusicManager man = getGuildAudioPlayer(sendee.getMessage().getGuild());
			TrackScheduler scheduler = (TrackScheduler) man.getScheduler();
			scheduler.shuffle();
		}
    }

    private void loadAndPlay(final Message message, final String trackUrl, boolean shuffle, boolean preserveplaylist) {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        TrackScheduler scheduler = (TrackScheduler) musicManager.getScheduler();
        GuildManager.DEFAULT_PLAYER_MANAGER.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                message.getTextChannel().sendMessage(String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author)).queue();
                Map<String, Object> meta = scheduler.getTrackMeta(track);
                meta.put("tchan", message.getTextChannel().getId());
                meta.put("requester", message.getAuthor().getId());
                scheduler.queue(track);
				System.out.println("LOAD HASH: " + scheduler.hashCode());
			}

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult() && !preserveplaylist) {
                    trackLoaded(playlist.getTracks().get(0));
                } else {
                    if (shuffle) Collections.shuffle(playlist.getTracks());
                    playlist.getTracks().forEach(audioTrack -> {
                        Map<String, Object> meta = scheduler.getTrackMeta(audioTrack);
                        meta.put("tchan", message.getTextChannel().getId());
                        meta.put("requester", message.getAuthor().getId());
                        scheduler.queue(audioTrack);
                    });
                    message.getChannel().sendMessage(String.format("Added `%s` tracks to the queue for you from `%s`. :3 %s", playlist.getTracks().size(), playlist.getName(), shuffle ? "randomized!" : "")).queue();
                }
            }

            @Override
            public void noMatches() {
                if (trackUrl.startsWith("ytsearch:"))
                    message.getTextChannel().sendMessage("Nothing found by `" + trackUrl.replace("ytsearch: ", "") + "`.").queue();
                else
                    loadAndPlay(message, "ytsearch:" + trackUrl, shuffle, preserveplaylist);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                message.getTextChannel().sendMessage(format("Sorry, failed to load that track because of a { %s }", exception.getClass())).queue();
                LogManager.TOHSAKA.error(format("Failed to load track in guild { %s#%s } due to a { %s }.", message.getGuild().getName(), message.getGuild().getId(), exception.getClass()), exception);
            }
        });
	}

    private GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildManager manager = GuildManager.getManagerFor(guild.getId());
        if(manager == null) {
        	System.out.println("Creating guild manager.");
            manager = GuildManager.createManagerFor(guild);
			TrackScheduler scheduler = new TrackScheduler(manager.getAudioManager().getPlayer(), guild);
			manager.getAudioManager().addEventListener(scheduler);
        }
        return manager.getAudioManager();
    }

}
