package com.nachtraben.tohsaka.commands.botadministration;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;

import java.util.Map;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown", "", "Shuts down the bot.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            long id = sendee.getUser().getIdLong();
            BotConfig config = Tohsaka.getInstance().getConfig();
            if (!config.getOwnerIDs().contains(id) && !config.getDeveloperIDs().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't shut me down.");
            }
            sendee.sendMessage(ChannelTarget.GENERIC, "Goodbye!");
            Tohsaka.getInstance().shutdown();
        } else {
            sender.sendMessage("Goodbye!");
            Tohsaka.getInstance().shutdown();
        }
    }
}
