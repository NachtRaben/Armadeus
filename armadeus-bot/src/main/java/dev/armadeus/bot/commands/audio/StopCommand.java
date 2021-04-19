package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;

public class StopCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("stop")
    public void stop(DiscordUser user) {
        GuildMusicManager manager = user.getGuildMusicManager();
        if (manager.getLink().getChannel() != null) {
            manager.getScheduler().stop();
            manager.getLink().destroy();
        }
    }
}
