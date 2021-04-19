package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.TimeUtil;

public class SeekCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("seek|buffer")
    public void seek(DiscordUser user, String time) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();
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
