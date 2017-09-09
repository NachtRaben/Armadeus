package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class AudioPlayCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioPlayCommand.class);

    public AudioPlayCommand() {
        super("play", "{track}", "Plays the desired track, searching youtube if necessary.");
        super.setFlags(Arrays.asList("-pr", "--random", "--randomize", "--shuffle", "--playlist"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(!sendee.getGuild().getAudioManager().isConnected() && sendee.getVoiceChannel() == null) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry but you have to be in a voice channel to play music.");
                return;
            }
            boolean shuffle = flags.containsKey("r") || flags.containsKey("random") || flags.containsKey("randomize") || flags.containsKey("shuffle");
            boolean playlist = flags.containsKey("p") || flags.containsKey("playlist");
            loadAndPlay(sendee, args.get("track"), shuffle, playlist);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private void loadAndPlay(GuildCommandSender sender, String search, boolean shuffle, boolean preservePlaylist) {
        GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sender.getGuild().getIdLong()).getMusicManager();
        manager.getPlayerManager().loadItemOrdered(manager, search, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                sender.sendMessage(ChannelTarget.MUSIC, String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
                track.setUserData(sender);
                manager.getScheduler().queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(playlist.isSearchResult() && !preservePlaylist) {
                    trackLoaded(playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0));
                } else {
                    if(shuffle)
                        Collections.shuffle(playlist.getTracks());
                    playlist.getTracks().forEach(track -> {
                        track.setUserData(sender);
                        manager.getScheduler().queue(track);
                    });
                    sender.sendMessage(ChannelTarget.MUSIC, String.format("Adding `%s` tracks to the queue from `%s`. :3%s.", playlist.getTracks().size(), playlist.getName(), shuffle ? " shuffled!" : ""));
                }
            }

            @Override
            public void noMatches() {
                if(search.startsWith("ytsearch:"))
                    sender.sendMessage("Failed to find any results for `" + search.replace("ytsearch:", "") + "`.");
                else
                    loadAndPlay(sender, "ytsearch:" + search, shuffle, preservePlaylist);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sender.sendMessage(ChannelTarget.MUSIC, "Failed to load the track, `" + exception.getMessage().replace("`", "") + "`.");
                LOGGER.warn("An exception occurred while searching for a track.", exception);
            }
        });
    }

}
