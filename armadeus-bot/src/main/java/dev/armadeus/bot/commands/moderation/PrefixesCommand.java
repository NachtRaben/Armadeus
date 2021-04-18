package dev.armadeus.bot.commands.moderation;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.configuration.GuildConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;

import java.util.Arrays;
import java.util.Map;

public class PrefixesCommand extends Command {

    public PrefixesCommand() {
        super("prefixes", "", "Shows the prefixes for this guild.");
        super.setAliases(Arrays.asList("prefix"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildConfig config = sendee.getGuildConfig();
            if (config.getPrefixes() != null && !config.getPrefixes().isEmpty()) {
                sendee.sendMessage(ChannelTarget.GENERIC, "The prefixes are: `" + config.getPrefixes() + "`");
                return;
            } else {
                sendee.sendMessage(ChannelTarget.GENERIC, "The prefixes are: `" + BotConfig.get().getGlobalPrefixes() + "`");
            }
        }
        sender.sendMessage("The prefixes are: `" + BotConfig.get().getGlobalPrefixes() + "`");
    }
}
