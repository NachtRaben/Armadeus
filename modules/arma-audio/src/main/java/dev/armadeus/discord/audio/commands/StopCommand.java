package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

public class StopCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("stop|disconnect|leave")
    @CommandPermission("armadeus.stop")
    public void stop(DiscordCommandIssuer user) {
        AudioManager manager = getAudioManager(user);
        if (manager.getPlayer().getLink().getChannelId() != -1) {
            manager.getScheduler().stop();
//            manager.getPlayer().getLink().destroy();
        }
    }
}
