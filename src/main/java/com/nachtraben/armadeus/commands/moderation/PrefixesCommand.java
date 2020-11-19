package com.nachtraben.armadeus.commands.moderation;

import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;

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
                sendee.sendMessage(ChannelTarget.GENERIC, "The prefixes are: `" + Armadeus.getInstance().getConfig().getPrefixes() + "`");
            }
        }
        sender.sendMessage("The prefixes are: `" + com.nachtraben.armadeus.Armadeus.getInstance().getConfig().getPrefixes() + "`");
    }
}
