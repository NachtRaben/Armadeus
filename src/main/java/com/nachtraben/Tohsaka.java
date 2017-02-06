package com.nachtraben;

import com.nachtraben.commands.AdminCommands;
import com.nachtraben.commands.AudioCommands;
import com.nachtraben.commands.MiscCommands;
import com.xilixir.fw.BotFramework;
import com.xilixir.fw.command.sender.UserCommandSender;
import com.xilixir.fw.utils.ConsoleCommandImpl;
import com.xilixir.fw.utils.LogManager;
import com.xilixir.fw.utils.StringUtils;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class Tohsaka extends BotFramework {

    public static Tohsaka instance;

    public Tohsaka(boolean debug) {
        super(LogManager.TOHSAKA);
        BotFramework.debug = true;
        //if (BotFramework.debug) BotFramework.COMMAND_PREFIXES = Arrays.asList("-", "?");
        super.addCommandResultEvent(result -> {
            switch (result.getResult()) {
                case SUCCESS:
                    if(result.getSender() instanceof UserCommandSender) {
                        UserCommandSender sender = (UserCommandSender) result.getSender();
                        LOGGER.info(StringUtils.format("{ %s#%s } issued bot command { %s::%s } in guild { %s#%s }",
                                sender.getName(),
                                sender.getUser().getDiscriminator(),
                                result.getProvidedCommandString(),
                                StringUtils.arrayToString(result.getProvidedArgs()),
                                sender.getGuild().getName(),
                                sender.getGuild().getId()));
                    } else {
                        LOGGER.info(StringUtils.format("%s issued bot command %s::%s", result.getSender().getName()));
                    }
                    break;
                case EXCEPTION:
                    LOGGER.error(StringUtils.format("{ %s } failed to run bot command { %s::%s } due to an encountered exception!",
                            result.getSender().getName(),
                            result.getProvidedCommandString(),
                            StringUtils.arrayToString(result.getProvidedArgs())), result.getThrowable());
                    break;
                default:
                    if (result.getSender() instanceof UserCommandSender) {
                        UserCommandSender sender = (UserCommandSender) result.getSender();
                        LOGGER.warn(StringUtils.format("{ %s } failed to run bot command  { %s::%s } in guild { %s#%s } due to a { %s }.",
                                result.getSender().getName(),
                                result.getProvidedCommandString()),
                                StringUtils.arrayToString(result.getProvidedArgs()),
                                sender.getGuild().getName(),
                                sender.getGuild().getId(),
                                result.getResult());
                    } else {
                        LOGGER.warn(StringUtils.format("{ %s } failed to run bot command  { %s::%s } due to a { %s }.",
                                result.getSender().getName(),
                                result.getProvidedCommandString(),
                                StringUtils.arrayToString(result.getProvidedArgs()),
                                result.getResult()));
                    }
            }
        });
        registerCommands(new AdminCommands());
        registerCommands(new AudioCommands());
        registerCommands(new MiscCommands());
        ConsoleCommandImpl.instance.start();
        super.initJDAs();
    }


    public static void main(String[] args) {
        new Tohsaka(false);
    }

}
