package com.nachtraben.armadeus.commands.botadministration;

import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;

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
            BotConfig config = Armadeus.getInstance().getConfig();
            if (!config.getOwnerIDs().contains(id) && !config.getDeveloperIDs().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't shut me down.");
                return;
            }
            sendee.sendMessage(ChannelTarget.GENERIC, "Goodbye!");
            com.nachtraben.armadeus.Armadeus.getInstance().shutdown();
        } else {
            sender.sendMessage("Goodbye!");
            com.nachtraben.armadeus.Armadeus.getInstance().shutdown();
        }
    }
}
