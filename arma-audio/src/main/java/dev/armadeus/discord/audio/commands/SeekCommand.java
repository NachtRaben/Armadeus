package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import lavalink.client.player.track.AudioTrack;

public class SeekCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("seek|buffer")
    @CommandPermission("armadeus.seek")
    @Description("Seek to a position in the currently playing track")
    public void seek(DiscordCommandIssuer user, String time) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);
        AudioTrack current = manager.getPlayer().getPlayingTrack();
        if (current.getInfo().isStream()) {
            user.sendMessage("Sorry, but this track is not seekable");
            return;
        }

        boolean seek = time.startsWith("+") || time.startsWith("-");
        long position = Math.abs(TimeUtil.parse(time.replaceAll("[+-]", "")));

        if (seek)
            position = time.startsWith("+") ? manager.getPlayer().getTrackPosition() + position : manager.getPlayer().getTrackPosition() - position;
        else
            position = Math.max(0, Math.min(position, current.getInfo().getLength()));

        manager.getPlayer().seekTo(position);
        user.sendMessage("Seeking to `" + TimeUtil.format(position) + "/" + TimeUtil.format(current.getInfo().getLength()) + "`");
    }
}
