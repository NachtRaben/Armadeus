package com.nachtraben.tohsaka;


import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.CommandEvent;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.ConsoleCommandImpl;
import com.nachtraben.tohsaka.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class Tohsaka extends JDABot {

	private static final Logger logger = LoggerFactory.getLogger(Tohsaka.class);
    public static boolean debug;

    public Tohsaka(boolean debug) {
        super.loadJDAs();
        Tohsaka.debug = debug;
        getCommandHandler().addEventListener((e) -> {
        	if(e.getResult().equals(CommandEvent.Result.EXCEPTION)) {
        		logger.info(e.toString());
        		logger.info(e.getThrowable().getMessage(), e.getThrowable());
			} else {
        		if(e.getResult() != CommandEvent.Result.COMMAND_NOT_FOUND && e.getSender() instanceof GuildCommandSender) {
        			GuildCommandSender sender = (GuildCommandSender) e.getSender();
        			GuildManager manager = GuildManager.getManagerFor(sender.getGuild());
        			if(manager.getConfig().shouldDeleteCommandMessages())
        				sender.getMessage().delete().queue();

				}
				logger.debug(e.toString());
			}
		});
        super.getCommandHandler().registerCommands(new OwnerCommands());
        super.getCommandHandler().registerCommands(new AdminCommands());
        super.getCommandHandler().registerCommands(new AudioCommands());
        super.getCommandHandler().registerCommands(new MiscCommands());
		super.getCommandHandler().registerCommands(new LogChannelCommands());
        super.getCommandHandler().registerCommands(new CatGirlsCommand());
        super.getCommandHandler().registerCommands(new GuildOwnerCommands());
        ConsoleCommandImpl.instance.start();
    }

    public static void main(String[] args) {
        new Tohsaka(false);
    }

}
