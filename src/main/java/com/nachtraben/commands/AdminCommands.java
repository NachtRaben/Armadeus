package com.nachtraben.commands;

import com.nachtraben.Tohsaka;
import com.xilixir.fw.command.Command;
import com.xilixir.fw.command.sender.CommandSender;
import com.xilixir.fw.command.sender.ConsoleCommandSender;
import com.xilixir.fw.command.sender.UserCommandSender;

import java.util.Map;

import static com.xilixir.fw.utils.StringUtils.format;


/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AdminCommands {

    public AdminCommands() {

    }

    @Command(name = "invite", format = "", description = "Generates the invite link for the bot.")
    public void invite(UserCommandSender sender, Map<String, String> args) {
        sender.getTextChannel().sendMessage(format("Here is my invite link! https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s", sender.getCommandMessage().getJDA().getSelfUser().getId(), "2146958463")).queue();
    }

    @Command(name = "invite", format = "", description = "Generates the invite link for the bot.")
    public void invite(ConsoleCommandSender sender, Map<String, String> args) {
        sender.sendMessage(format("CONSOLE: Here is my invite link! https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s", Tohsaka.instance.getJDA(0).getSelfUser().getId(), "2146958463"));
    }

    @Command(name = "shutdown", format = "", description = "Shuts down Tohsaka.")
    public void onShutdown(CommandSender sender, Map<String, String> args) {
        Tohsaka.getInstance().stop();
    }

    @Command(name = "test", format = "", description = "")
    public void test(UserCommandSender s) {
    }

}
