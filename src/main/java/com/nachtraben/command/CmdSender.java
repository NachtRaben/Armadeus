package com.nachtraben.command;

import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.command.sender.UserCommandSender;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public enum CmdSender {
    USER, CONSOLE, BOTH;

    public static CmdSender valueOf(CommandSender sender) {
        if(sender instanceof ConsoleCommandSender) return CmdSender.CONSOLE;
        else if(sender instanceof UserCommandSender) return CmdSender.USER;
        else return CmdSender.BOTH;
    }
}
