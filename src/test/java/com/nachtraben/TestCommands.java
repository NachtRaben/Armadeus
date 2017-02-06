package com.nachtraben;

import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.commandrework.Command;

import java.util.Map;


/**
 * Created by NachtRaben on 2/4/2017.
 */
public class TestCommands {

    @Command(name = "test", aliases = { "uno", "dos" }, format = "<test> [optional]", description = "Echos the statement!", flags = { "-fbcd", "--format=", "--randomize"})
    public boolean test(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        System.out.println("TEST was ran");
        return true;
    }

}
