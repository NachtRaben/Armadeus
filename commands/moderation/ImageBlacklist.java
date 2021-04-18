package dev.armadeus.bot.commands.moderation;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageBlacklist {

    @Cmd(name = "blacklist", format = "<url>", description = "Blacklists URL's from the image commands.")
    public void blacklist(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            long id = sendee.getUser().getIdLong();
            BotConfig config = BotConfig.get();
            if (!config.getOwnerIds().contains(id) && !config.getDeveloperIds().contains(id) && id==sendee.getGuild().getOwnerIdLong()) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't shut me down.");
                return;
            }

            throw new UnsupportedOperationException();
//            List<String> urls = (List<String>) config.getMetadata().computeIfAbsent("blacklisted-urls", k -> new ArrayList<>());
//            if (!urls.contains(args.get("url")))
//                urls.add(args.get("url"));
//            config.save();
//            sendee.sendMessage("The url has been added to the blacklist.");
        }
    }

}
