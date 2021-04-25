package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

@CommandAlias("repeat")
public class RepeatCommand extends AudioCommand {

    @Conditions("guildonly")
    @Subcommand("track")
    public void repeatTrack(DiscordCommandIssuer user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);

        manager.getScheduler().setRepeatTrack(repeat);
        if (repeat) {
            user.sendMessage("I will now repeat `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`");
        } else {
            user.sendMessage("I will no longer repeat the track");
        }
    }

    @Conditions("guildonly")
    @Subcommand("queue")
    public void repeatQueue(DiscordCommandIssuer user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);
        if (!manager.getScheduler().isPlaying()) {
            user.sendMessage("There is currently nothing playing. Queue a song with the `play` command.");
            return;
        }

        manager.getScheduler().setRepeatQueue(repeat);
        if (repeat) {
            user.sendMessage("I will now repeat the queue");
        } else {
            user.sendMessage("I will no longer repeat the queue");
        }
    }
}
