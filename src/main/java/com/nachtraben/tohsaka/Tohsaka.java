package com.nachtraben.tohsaka;


import com.nachtraben.commandapi.CommandEvent;
import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.ConsoleCommandImpl;
import com.nachtraben.tohsaka.commands.*;
import com.nachtraben.tohsaka.commands.admin.AdminCommands;
import com.nachtraben.tohsaka.commands.admin.ConfCommands;
import com.nachtraben.tohsaka.commands.audio.AudioCommands;
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
        getCommandHandler().addEventListener((event) -> {
            if(event.getResult().equals(CommandEvent.Result.EXCEPTION)) {
                logger.info(event.toString());
                logger.info(event.getThrowable().getMessage(), event.getThrowable());
            } else {
                if(event.getResult() != CommandEvent.Result.COMMAND_NOT_FOUND && event.getSender() instanceof GuildCommandSender) {
                    GuildCommandSender sender = (GuildCommandSender) event.getSender();
                    logger.debug(String.format("%s#%s executed command { %s } in { %s#%s }.", sender.getName(), sender.getUser().getDiscriminator(), event.getCommand().getName(), sender.getGuild().getName(), sender.getGuild().getId()));
                    GuildManager manager = GuildManager.getManagerFor(sender.getGuild());
                    if(manager.getConfig().shouldDeleteCommandMessages())
                        sender.getMessage().delete().queue();
                } else {
                    logger.debug(event.toString());
                }
            }
        });

        super.getCommandHandler().registerCommands(new OwnerCommands());
        super.getCommandHandler().registerCommands(new AdminCommands());
        super.getCommandHandler().registerCommands(new AudioCommands());
        super.getCommandHandler().registerCommands(new MiscCommands());
        super.getCommandHandler().registerCommands(new LogChannelCommands());
        super.getCommandHandler().registerCommands(new CatGirlsCommand());
        super.getCommandHandler().registerCommands(new GuildOwnerCommands());
        super.getCommandHandler().registerCommands(new HelpCommand());
        super.getCommandHandler().registerCommands(new ConfCommands());
        super.getCommandHandler().registerCommands(new WebTest());

        ConsoleCommandImpl.instance.start();
    }

    public static void main(String[] args) {
        new Tohsaka(false);
    }

}
