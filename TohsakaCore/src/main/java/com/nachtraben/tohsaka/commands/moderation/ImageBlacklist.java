package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImageBlacklist {

    @Cmd(name = "blacklist", format = "<url>", description = "Blacklists URL's from the image commands.")
    public void blacklist(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            long id = sendee.getUser().getIdLong();
            BotConfig config = Tohsaka.getInstance().getConfig();
            if (!config.getOwnerIDs().contains(id) && !config.getDeveloperIDs().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't shut me down.");
                return;
            }

            Set<String> urls = (Set<String>) config.getMetadata().computeIfAbsent("blacklisted-urls", k -> new HashSet<String>());
            urls.add(args.get("url"));
            config.save();
            sendee.sendMessage("The url has been added to the blacklist.");
        }
    }

}
