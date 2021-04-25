package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Optional;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.StringUtils;
import dev.armadeus.discord.audio.AudioManager;

import java.util.List;

public class SkipCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("skip")
    public void skip(DiscordCommandIssuer user, @Optional String track) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);
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
            if (StringUtils.tokenCompare(t.getInfo().title, tokens)) {
                manager.getScheduler().skipTo(t);
                return;
            }
        }
        user.sendMessage("Sorry but I couldn't find a track in queue similar to `" + track + "`");
    }
}



