package com.nachtraben.tohsaka;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.command.ConsoleCommandSender;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.orangeslice.event.CommandEventListener;
import com.nachtraben.orangeslice.event.CommandExceptionEvent;
import com.nachtraben.orangeslice.event.CommandPostProcessEvent;
import com.nachtraben.orangeslice.event.CommandPreProcessEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class Tohsaka extends DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tohsaka.class);

    private static Tohsaka instance;

    public Tohsaka(String[] args, boolean debugging) {
        super(args);
        instance = this;

        setDebugging(debugging);
        new ConsoleCommandSender();
        getShardManager().connectAllShards();
        registerCommands();
        getCommandBase().registerEventListener(new CommandEventListener() {
            @Override
            public void onCommandPreProcess(CommandPreProcessEvent e) {
                LOGGER.debug(String.format("CommandPreProcess >> Sender: %s, Args: %s, Flags: %s, Command: %s", e.getSender().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName()));
            }

            @Override
            public void onCommandPostProcess(CommandPostProcessEvent event) {
                if(event.getSender() instanceof GuildCommandSender) {
                    GuildCommandSender sender = (GuildCommandSender) event.getSender();
                    Member bot = sender.getGuild().getMember(sender.getGuild().getJDA().getSelfUser());
                    if(getGuildManager().getConfigurationFor(sender.getGuild()).shouldDeleteCommands() && bot.hasPermission(Permission.MESSAGE_MANAGE))
                        sender.getMessage().delete().reason("Command message.").queue();
                }
            }

            @Override
            public void onCommandException(CommandExceptionEvent e) {
                e.getSender().sendMessage("Unfortunately an error has occurred with your request. The bot author has been notified.");
                //LOGGER.error("An error occurred during command execution.", e.getException());
            }
        });
    }

    private void registerCommands() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.nachtraben.tohsaka.commands"))
                        .setScanners(
                                new SubTypesScanner(),
                                new MethodAnnotationsScanner()
                        )
        );
        Set<Class<?>> classes = new HashSet<>();
        for (Method m : reflections.getMethodsAnnotatedWith(Cmd.class)) {
            if (!classes.contains(m.getDeclaringClass())) {
                classes.add(m.getDeclaringClass());
                try {
                    getCommandBase().registerCommands(m.getDeclaringClass().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.error("Failed to register command class, " + m.getDeclaringClass() + ".", e);
                }
            }
        }
        for (Class s : reflections.getSubTypesOf(Command.class)) {
            try {
                getCommandBase().registerCommands(s.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Tohsaka getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        new Tohsaka(args, false);
    }

}
