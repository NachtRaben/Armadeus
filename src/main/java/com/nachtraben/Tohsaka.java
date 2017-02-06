package com.nachtraben;

import com.nachtraben.commands.AdminCommands;
import com.nachtraben.commands.AudioCommands;
import com.nachtraben.commands.MiscCommands;
import com.xilixir.fw.BotFramework;
import com.xilixir.fw.utils.LogManager;

import static com.xilixir.fw.utils.Utils.format;

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
            if (!result.succeeded()) {
                switch (result.getResult()) {
                    case FAILURE:
                        LOGGER.info("Failed to run " + result.getCommand());
                        break;
                    case INVALID_ARGS:
                        LOGGER.info("Invalid args for " + result.getCommand());
                        break;
                    case INVALID_FLAGS:
                        LOGGER.info("Invalid flags for " + result.getCommand());
                        break;
                    case UNKNOWN_COMMAND:
                        LOGGER.info("Unknown command, " + result.getCommandString());
                        break;
                    case NO_PERMISSION:
                        LOGGER.info("Missing permissions for " + result.getCommand());
                        break;
                    case EXCEPTION:
                        LOGGER.error(format("Failed to run command { %s::%s } in { %s#%s }.", result.getCommand(), result.getArgs(), null, null), result.getStack());
                        break;
                }
            }
        });
        registerCommands(new AdminCommands());
        registerCommands(new AudioCommands());
        registerCommands(new MiscCommands());
        super.initJDAs();
    }


    public static void main(String[] args) {
        new Tohsaka(false);
    }

}
