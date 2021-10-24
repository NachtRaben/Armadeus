package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

public class EqualizerCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("equalizer|eq")
    @CommandPermission("armadeus.volume")
    @Description("Adjusts the equalizer modifier")
    public void volume(DiscordCommandIssuer user, float modifier) {
        if (modifier < 0.1f || cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);
        if (modifier > 0.1f) {
            manager.updateEqualizerModifier(modifier);
        }
        user.sendMessage(String.format("The equalizer modifier is currently `%.2f`%%", manager.getEqualizerModifier()));
    }
}
