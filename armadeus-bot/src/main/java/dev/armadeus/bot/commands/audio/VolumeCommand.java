package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Optional;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;

public class VolumeCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("volume|vol")
    public void volume(DiscordUser user, @Optional Integer volume) {
        if (cannotQueueMusic(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();
        if (volume != null) {
            float vol = Math.max(0.0f, Math.min(1.0f, volume.floatValue() / 100.0f));
            manager.setVolume(vol);
        }
        user.sendMessage("The volume is currently `" + manager.getPlayer().getFilters().getVolume() * 100.0f + "`%");
    }
}
