package dev.armadeus.discord.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;

public class ShutdownCommand extends DiscordCommand {

    @Conditions("developeronly")
    @CommandAlias("shutdown")
    public void shutdown(DiscordCommandIssuer user) {
        // TODO: Implement shutdown
        System.exit(0);
//        ArmaCore.get().shutdown();
    }
}
