package dev.armadeus.bot.api.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Dependency;
import dev.armadeus.bot.api.ArmaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiscordCommand extends BaseCommand {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Dependency
    protected ArmaCore core;

}
