package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.core.command.DiscordUser;

public class ShuffleCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("shuffle")
    public void shuffle(DiscordUser user) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        user.getGuildMusicManager().getScheduler().shuffle();
        user.sendMessage("The queue has been shuffled! ~owo~");
    }
}
