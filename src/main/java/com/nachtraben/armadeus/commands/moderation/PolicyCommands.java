package com.nachtraben.armadeus.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.command.SubCommand;
import net.dv8tion.jda.api.Permission;

import java.util.Map;

public class PolicyCommands extends CommandTree {

    public PolicyCommands() {
        getChildren().add(new SubCommand("conf", "get policy", "Gets the policy for disabled commands.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = sendee.getDbot().getConfig();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if (!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIDs().contains(sendee.getUserId()) && !botConfig.getDeveloperIDs().contains(sendee.getUserId())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you don't have permission to use that command.");
                        return;
                    }
                    sendee.sendMessage("The current policy is: `" + (guildConfig.isBlacklist() ? "BLACKLIST`" : "WHITELIST`"));
                }
            }
        });
        getChildren().add(new SubCommand("conf", "set policy <blacklist/whitelist>", "Sets the policy for disabled commands.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = sendee.getDbot().getConfig();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if (!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIDs().contains(sendee.getUserId()) && !botConfig.getDeveloperIDs().contains(sendee.getUserId())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you don't have permission to use that command.");
                        return;
                    }
                    boolean isBlacklisted = args.get("blacklist/whitelist").equalsIgnoreCase("blacklist");
                    guildConfig.setBlacklist(isBlacklisted);
                    guildConfig.save();
                    sendee.sendMessage("The current policy is now: `" + (guildConfig.isBlacklist() ? "BLACKLIST`" : "WHITELIST`"));
                }
            }
        });
    }

}
