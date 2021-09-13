package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;


public class VolumeCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("volume|vol")
    @CommandPermission("armadeus.volume")
    public void volume(DiscordCommandIssuer user, @Optional Integer volume) {
        if (volume != null && cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);
        if (volume != null) {
            manager.setVolume(volume.floatValue() / 100.0f);
        }
        user.sendMessage(String.format("The volume is currently `%.2f`%%", manager.getPlayer().getFilters().getVolume() * 100.0f));
    }
}
