package dev.armadeus.bot.commands.botadministration;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;

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
            BotConfig config = BotConfig.get();
            if (!config.getOwnerIds().contains(id) && !config.getDeveloperIds().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't shut me down.");
                return;
            }
            sendee.sendMessage(ChannelTarget.GENERIC, "Goodbye!");
            Armadeus.getInstance().shutdown();
        } else {
            sender.sendMessage("Goodbye!");
            Armadeus.getInstance().shutdown();
        }
    }
}
