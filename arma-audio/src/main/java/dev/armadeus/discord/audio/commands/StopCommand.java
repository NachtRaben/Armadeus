package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

public class StopCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("stop|disconnect|leave")
    @CommandPermission("armadeus.stop")
    @Description("Stops all audio playback and disconnects from the channel")
    public void stop(DiscordCommandIssuer user) {
        user.sendMessage("Stopping audio playback");
        AudioManager manager = getAudioManager(user);
        if (manager.getPlayer().getLink().getChannel() != null) {
            manager.getScheduler().stop();
            manager.getPlayer().getLink().destroy();
        }
    }
}
