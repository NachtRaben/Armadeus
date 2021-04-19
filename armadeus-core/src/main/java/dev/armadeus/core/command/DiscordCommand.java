package dev.armadeus.core.command;

import co.aikar.commands.BaseCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DiscordCommand extends BaseCommand {

    protected Logger logger = LogManager.getLogger(getClass());

}
