package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class AudioPlayCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(AudioPlayCommand.class);

    public AudioPlayCommand() {
        super("play", "{track}", "Plays the desired track, searching youtube if necessary.");
        super.setFlags(Arrays.asList("-r", "--random", "--randomize", "--shuffle", "--limit=", "--all"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = sendee.getGuildConfig().getMusicManager();
            if (manager.getLink().getChannel() == null && sendee.getVoiceChannel() == null) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry but you have to be in a voice channel to play music.");
                return;
            }
            boolean shuffle = flags.containsKey("r") || flags.containsKey("random") || flags.containsKey("randomize") || flags.containsKey("shuffle");
            int playlistLimit = flags.containsKey("all") ? -1 : Integer.parseInt(flags.getOrDefault("limit", "10"));
            loadAndPlay(sendee, args.get("track"), shuffle, playlistLimit);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private void trackLoaded(AudioTrack track, GuildCommandSender sender) {
        sender.sendMessage(ChannelTarget.MUSIC, String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
        track.setUserData(sender);
        sender.getGuildConfig().getMusicManager().getScheduler().queue(track);
    }

    private void playlistLoaded(AudioPlaylist playlist, GuildCommandSender sender, boolean shuffle, int playlistLimit) {
        if (shuffle)
            Collections.shuffle(playlist.getTracks());
        int start = playlist.getTracks().indexOf(playlist.getSelectedTrack());
        int loaded = 0;
        for (int i = 0; i < playlistLimit; i++) {
            AudioTrack track = playlist.getTracks().get(start + i);
            if (track == null) {
                break;
            }
            track.setUserData(sender);
            sender.getGuildConfig().getMusicManager().getScheduler().queue(track);
        }
        if (playlistLimit > 1) {
            sender.sendMessage(ChannelTarget.MUSIC, String.format("Adding `%s` tracks to the queue from `%s`. :3%s.", loaded, playlist.getName(), shuffle ? " shuffled!" : ""));
        }
    }

    private void loadAndPlay(GuildCommandSender sender, String search, boolean shuffle, int playlistLimit) {
        boolean isSearch = search.startsWith("ytsearch:");
        if (isSearch) {
            sender.getGuildConfig().getMusicManager().getLink().getRestClient().getYoutubeSearchResult(search).thenAccept(tracks -> {
                if (tracks.isEmpty()) {
                    sender.sendMessage("Failed to find any results for `" + search.replace("ytsearch:", "") + "`.");
                    return;
                }
                AudioPlaylist playlist = new BasicAudioPlaylist("Search Results", tracks, tracks.get(0), true);
                playlistLoaded(playlist, sender, shuffle, playlistLimit);
            });
        } else {
            sender.getGuildConfig().getMusicManager().getLink().getRestClient().loadItem(search, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    AudioPlayCommand.this.trackLoaded(track, sender);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioPlayCommand.this.playlistLoaded(playlist, sender, shuffle, playlistLimit);
                }

                @Override
                public void noMatches() {
                    loadAndPlay(sender, "ytsearch:" + search, shuffle, 1);
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    sender.sendMessage(ChannelTarget.MUSIC, "Failed to load the track, `" + exception.getMessage().replace("`", "") + "`.");
                    log.warn("An exception occurred while searching for a track.", exception);
                }
            });
        }
    }

}
