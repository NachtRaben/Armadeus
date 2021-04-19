package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;

@CommandAlias("repeat")
public class RepeatCommand extends AudioCommand {

    @Conditions("guildonly")
    @Subcommand("track")
    public void repeatTrack(DiscordUser user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();

        manager.getScheduler().setRepeatTrack(repeat);
        if (repeat) {
            user.sendMessage("I will now repeat `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`");
        } else {
            user.sendMessage("I will no longer repeat the track");
        }
    }

    @Conditions("guildonly")
    @Subcommand("queue")
    public void repeatQueue(DiscordUser user, @Default(value = "true") boolean repeat) {
        if (cannotQueueMusic(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();
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
