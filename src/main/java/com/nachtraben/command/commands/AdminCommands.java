package com.nachtraben.command.commands;

import com.nachtraben.Tohsaka;
import com.nachtraben.command.Cmd;
import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.command.sender.UserCommandSender;
import com.nachtraben.log.LogManager;

import java.util.Map;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AdminCommands {

    public AdminCommands() {
    }

    @Cmd(name = "invite", format = "invite", description = "Generates the bots invite link.")
    public void invite(UserCommandSender sender, Map<String, String> args) {
        sender.getCommandMessage().getTextChannel().sendMessage(format("Here is my invite link! https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s", sender.getCommandMessage().getJDA().getSelfUser().getId(), "2146958463")).queue();
    }

    @Cmd(name = "invite", format = "invite", description = "Generates the bots invite link.")
    public void invite(ConsoleCommandSender sender, Map<String, String> args) {
        sender.sendMessage(format("CONSOLE: Here is my invite link! https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s", Tohsaka.instance.getJDA(0).getSelfUser().getId(), "2146958463"));
    }

    @Cmd(name = "shutdown", format = "shutdown", description = "Shuts down the bot.")
    public void onShutdown(CommandSender sender, Map<String, String> args) {
        Tohsaka.instance.stop();
    }

    @Cmd(name = "test", format = "test moo <arg1> <arg2> [arg3]", description = "Fires a test warning.")
    public void test(CommandSender sender, Map<String, String> args) {
        LogManager.TOHSAKA.info("arg1: %s\targ2: %s\t arg3: %s", args.get("arg1"), args.get("arg2"), args.get("arg3"));
    }
}
