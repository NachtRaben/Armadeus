package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Optional;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

public class VolumeCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("volume|vol")
    @CommandPermission("armadeus.volume")
    public void volume(DiscordCommandIssuer user, @Optional Integer volume) {
        if (cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);
        if (volume != null) {
            manager.setVolume(volume.floatValue() / 100.0f);
        }
        user.sendMessage("The volume is currently `" + manager.getPlayer().getFilters().getVolume() * 100.0f + "`%");
    }
}
