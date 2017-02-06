package com.nachtraben.commands;

import com.nachtraben.audio.GuildMusicManager;
import com.nachtraben.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.xilixir.fw.command.Command;
import com.xilixir.fw.command.sender.CommandSender;
import com.xilixir.fw.command.sender.UserCommandSender;
import com.xilixir.fw.utils.HasteBin;
import com.xilixir.fw.utils.LogManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xilixir.fw.utils.Utils.format;

/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AudioCommands {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public AudioCommands() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    @Command(name = "play", format = "{target}", description = "Searches and attempts to play the provided song.")
    public void play(UserCommandSender sender, Map<String, String> args) {
        Message message = sender.getCommandMessage().getTextChannel().sendMessage("Searching for videos! Please give me a moment.").complete();
        String trackUrl = args.get("target");
        loadAndPlay(sender.getCommandMessage(), trackUrl);
    }

    @Command(name = "pause", format = "", description = "Toggles the pause status of the audio player.")
    public void pause(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager musicManager = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        musicManager.player.setPaused(!musicManager.player.isPaused());
    }

    @Command(name = "stop", format = "", description = "Stops audio playback.")
    public void stop(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager musicManager = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        musicManager.scheduler.stop();
    }

    @Command(name = "vol", format = "<volume>", description = "Changes the desired volume to a value between [0-150].")
    public boolean vol(CommandSender sender, Map<String, String> args) {
        UserCommandSender user = (UserCommandSender) sender;
        GuildMusicManager musicManager = getGuildAudioPlayer(user.getCommandMessage().getGuild());
        Integer i;
        try {
            i = Integer.parseInt(args.get("volume"));
        } catch (NumberFormatException e) {
            return false;
        }
        musicManager.player.setVolume(i);
        user.getCommandMessage().getTextChannel().sendMessage("Volume set to " + i + "/150.").queue();
        return true;
    }

    @Command(name = "skip", format = "", description = "Skips the currently playing track.")
    public void skip(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager musicManager = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        musicManager.scheduler.nextTrack();
    }

    @Command(name = "buffer", format = "<time>", description = "Buffers the currently playing track to the desired time.")
    public void buffer(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager musicManager = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        if (musicManager.player.getPlayingTrack().isSeekable()) {
            long time = TimeUtil.stringToMillis(args.get("time"));
            if (time > musicManager.player.getPlayingTrack().getDuration()) {
                sender.getCommandMessage().getTextChannel().sendMessage("You cannot buffer past " + TimeUtil.millisToString(musicManager.player.getPlayingTrack().getDuration(), TimeUtil.FormatType.STRING) + " for this track.");
                return;
            }
            sender.getCommandMessage().getTextChannel().sendMessage("Buffering to " + args.get("time") + ".");
            musicManager.player.getPlayingTrack().setPosition(time);
        } else {
            sender.getCommandMessage().getTextChannel().sendMessage("You cannot buffer a streamed track.");
        }
    }

    @Command(name = "queue", format = "", description = "Generates a hastebin of the current song queue.")
    public void queuelist(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager man = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        StringBuilder sb = new StringBuilder();
        if (man.player.getPlayingTrack() != null) {
            AudioTrackInfo info = man.player.getPlayingTrack().getInfo();
            sb.append("Current) ").append(info.title).append(" by ").append(info.author).append(" for ").append(TimeUtil.millisToString(info.length, TimeUtil.FormatType.STRING)).append(".\n");
        }
        List<AudioTrack> queue = man.scheduler.getQueueList();
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack audioTrack = queue.get(i);
            sb.append(i).append(") ").append(audioTrack.getInfo().title).append(" by ").append(audioTrack.getInfo().author).append(" for ").append(TimeUtil.millisToString(audioTrack.getInfo().length, TimeUtil.FormatType.STRING)).append(".\n");
        }
        HasteBin hastebin = new HasteBin(sb.toString());
        sender.getCommandMessage().getTextChannel().sendMessage("Here's the queue for ya! " + hastebin.getHaste()).queue();
    }

    @Command(name = "skipto", format = "{rest}", description = "Skips to the desired index in the queue.")
    public void queueto(UserCommandSender sender, Map<String, String> args) {
        boolean isIndex = false;
        int index = -1;
        String identifier = args.get("rest");
        if (identifier == null) return;
        try {
            index = Integer.parseInt(identifier);
            isIndex = true;
        } catch (Exception ignored) {
        }
        GuildMusicManager man = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        List<AudioTrack> queue = man.scheduler.getQueueList();
        if (isIndex && index > queue.size()) return;
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack track = queue.get(i);
            if (isIndex && i == index) {
                sender.getCommandMessage().getTextChannel().sendMessage(format("Skipping to queue index `%s`.", i)).queue();
                man.scheduler.skipTo(track);
                return;
            } else if (!isIndex && track.getInfo().title.toLowerCase().contains(identifier.toLowerCase())) {
                sender.getCommandMessage().getTextChannel().sendMessage(format("Skipping to queue index `%s`.", i)).queue();
                man.scheduler.skipTo(track);
                return;
            }
        }
        sender.getCommandMessage().getTextChannel().sendMessage(format("`%s` wasn't found anywhere in the queue!", identifier)).queue();
    }

    @Command(name = "repeat", format = "", description = "Sets the current track to repeat.")
    public void repeat(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager man = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        boolean repeat = !man.scheduler.repeat;
        man.scheduler.repeat = repeat;
        sender.getCommandMessage().getTextChannel().sendMessage(format("Repeating: %s", repeat)).queue();
    }

    @Command(name = "shuffle", format = "", description = "Shuffles the current song queue.")
    public void shuffle(UserCommandSender sender, Map<String, String> args) {
        GuildMusicManager man = getGuildAudioPlayer(sender.getCommandMessage().getGuild());
        man.scheduler.shuffle();
    }

    private void loadAndPlay(final Message message, final String trackUrl) {
        LogManager.TOHSAKA.debug("Made it to load and play!");
        String search = trackUrl;
        boolean randomize = trackUrl.contains("-randomize");
        boolean preserveSearchPlaylist = trackUrl.contains("-playlist");
        if (randomize) search = trackUrl.replace("-randomize", "");
        if (preserveSearchPlaylist) search = trackUrl.replace("-playlist", "");
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        playerManager.loadItemOrdered(musicManager, search, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                message.getTextChannel().sendMessage(String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author)).queue();
                Map<String, Object> meta = musicManager.scheduler.getTrackMeta(track);
                meta.put("tchan", message.getTextChannel().getId());
                meta.put("requester", message.getAuthor().getId());
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult() && !preserveSearchPlaylist) {
                    trackLoaded(playlist.getTracks().get(0));
                } else {
                    if (randomize) Collections.shuffle(playlist.getTracks());
                    playlist.getTracks().forEach(audioTrack -> {
                        Map<String, Object> meta = musicManager.scheduler.getTrackMeta(audioTrack);
                        meta.put("tchan", message.getTextChannel().getId());
                        meta.put("requester", message.getAuthor().getId());
                        musicManager.scheduler.queue(audioTrack);
                    });
                    message.getChannel().sendMessage(String.format("Added `%s` tracks to the queue for you from `%s`. :3 %s", playlist.getTracks().size(), playlist.getName(), randomize ? "randomized!" : "")).queue();
                }
            }

            @Override
            public void noMatches() {
                if (trackUrl.startsWith("ytsearch:"))
                    message.getTextChannel().sendMessage("Nothing found by `" + trackUrl.replace("ytsearch: ", "") + "`.").queue();
                else
                    loadAndPlay(message, "ytsearch:" + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                message.getTextChannel().sendMessage(format("Sorry, failed to load that track because of a { %s }", exception.getClass()));
                LogManager.TOHSAKA.error(format("Failed to load track in guild { %s#%s } due to a { %s }.", message.getGuild().getName(), message.getGuild().getId(), exception.getClass()), exception);
            }
        });
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager));
        musicManager.scheduler.guild = guild;
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

}
