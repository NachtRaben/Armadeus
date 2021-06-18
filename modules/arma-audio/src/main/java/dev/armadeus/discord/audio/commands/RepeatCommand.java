package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

@Conditions("guildonly")
@CommandAlias("repeat")
@CommandPermission("armadeus.repeat")
public class RepeatCommand extends AudioCommand {

    @Subcommand("track")
    public void repeatTrack(DiscordCommandIssuer user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);

        manager.getPlayer().getScheduler().setRepeatTrack(repeat);
        if (repeat) {
            user.sendMessage("I will now repeat `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`");
        } else {
            user.sendMessage("I will no longer repeat the track");
        }
    }

    @Subcommand("queue")
    public void repeatQueue(DiscordCommandIssuer user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);
        if (!manager.getPlayer().getScheduler().isPlaying()) {
            user.sendMessage("There is currently nothing playing. Queue a song with the `play` command.");
            return;
        }

        manager.getPlayer().getScheduler().setRepeatQueue(repeat);
        if (repeat) {
            user.sendMessage("I will now repeat the queue");
        } else {
            user.sendMessage("I will no longer repeat the queue");
        }
    }
}
