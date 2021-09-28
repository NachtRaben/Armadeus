package dev.armadeus.core.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;

public class ShutdownCommand extends DiscordCommand {

    @Private
    @Conditions("developeronly")
    @CommandAlias("shutdown")
    @Description("Developer command to shutdown the bot for reboot")
    public void shutdown(DiscordCommandIssuer user) {
        core.shutdown();
    }
}
