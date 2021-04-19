package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Optional;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;

import java.util.List;

import static dev.armadeus.bot.util.StringUtils.tokenCompare;

public class SkipCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("skip")
    public void skip(DiscordUser user, @Optional String track) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();
        if (track == null || track.isEmpty()) {
            manager.getScheduler().skip();
            return;
        }

        List<AudioTrack> tracks = manager.getScheduler().getQueue();
        try {
            int index = Integer.parseInt(track);
            manager.getScheduler().skipTo(tracks.get(Math.max(tracks.size() - 1, index)));
            return;
        } catch (NumberFormatException ignored) {
        }

        String[] tokens = track.split(" ");
        for (AudioTrack t : tracks) {
            if (tokenCompare(t.getInfo().title, tokens)) {
                manager.getScheduler().skipTo(t);
                return;
            }
        }
        user.sendMessage("Sorry but I couldn't find a track in queue similar to `" + track + "`");
    }
}



