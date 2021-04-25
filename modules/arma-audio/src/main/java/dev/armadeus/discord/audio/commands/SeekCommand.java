package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;

public class SeekCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("seek|buffer")
    public void seek(DiscordCommandIssuer user, String time) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);
        AudioTrack current = manager.getPlayer().getPlayingTrack();
        if (!current.isSeekable()) {
            user.sendMessage("Sorry, but this track is not seekable");
            return;
        }

        boolean seek = time.startsWith("+") || time.startsWith("-");
        long position = Math.abs(TimeUtil.parse(time.replaceAll("[+-]", "")));

        if (seek)
            position = time.startsWith("+") ? manager.getPlayer().getTrackPosition() + position : manager.getPlayer().getTrackPosition() - position;
        else
            position = Math.max(0, Math.min(position, current.getDuration()));

        manager.getPlayer().seekTo(position);
        user.sendMessage("Seeking to `" + TimeUtil.format(position) + "/" + TimeUtil.format(current.getDuration()) + "`");
    }
}
