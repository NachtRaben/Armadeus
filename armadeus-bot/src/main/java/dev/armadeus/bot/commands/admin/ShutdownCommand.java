package dev.armadeus.bot.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.DiscordCommand;
import dev.armadeus.core.command.DiscordUser;

public class ShutdownCommand extends DiscordCommand {

    @Conditions("developeronly")
    @CommandAlias("shutdown")
    public void shutdown(DiscordUser user) {
        Armadeus.get().shutdown();
    }
}
