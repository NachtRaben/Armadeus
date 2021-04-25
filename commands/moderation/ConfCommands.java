package dev.armadeus.bot.commands.moderation;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.configuration.GuildConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.TimeUtil;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConfCommands {

    private static final Logger logger = LogManager.getLogger();


    @Cmd(name = "conf", format = "set greeting <type> {greeting}", description = "Sets the greeting for the server.")
    public void setGreeting(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            GuildConfig config = sendee.getBot().getGuildManager().getConfigurationFor(sendee.getGuild());

            String action = args.get("type").toLowerCase();
            if(!action.equals("message") && !action.equals("dm")) {
                sender.sendMessage("Valid actions are `<Message/DM>`.");
                return;
            }

            String message = args.get("greeting").trim();

            config.getMetadata().put("welcome_action", action);
            config.getMetadata().put("welcome_message", message);
            sender.sendMessage("The welcome message is now, `" + message + "`.");
            config.save();

        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "conf", format = "remove greeting", description = "Removes the greeting for the server.")
    public void removeGreeting(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            GuildConfig config = sendee.getBot().getGuildManager().getConfigurationFor(sendee.getGuild());

            config.getMetadata().remove("welcome_action");
            config.getMetadata().remove("welcome_message");
            sender.sendMessage("The welcome message is now removed.");
            config.save();

        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private boolean hasPerms(GuildCommandSender sendee) {
        BotConfig botConfig = BotConfig.get();
        if (sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || botConfig.getDeveloperIds().contains(sendee.getUser().getIdLong()) || botConfig.getOwnerIds().contains(sendee.getUser().getIdLong())) {
            return true;
        }
        return true;
    }

}
