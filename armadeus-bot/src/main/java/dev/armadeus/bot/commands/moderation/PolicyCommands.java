package dev.armadeus.bot.commands.moderation;

import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.configuration.GuildConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.CommandTree;
import dev.armadeus.command.command.SubCommand;
import net.dv8tion.jda.api.Permission;

import java.util.Map;

public class PolicyCommands extends CommandTree {

    public PolicyCommands() {
        getChildren().add(new SubCommand("conf", "get policy", "Gets the policy for disabled commands.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = BotConfig.get();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if (!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIds().contains(sendee.getUser().getIdLong()) && !botConfig.getDeveloperIds().contains(sendee.getUser().getIdLong())) {
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
                    BotConfig botConfig = BotConfig.get();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if (!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIds().contains(sendee.getUser().getIdLong()) && !botConfig.getDeveloperIds().contains(sendee.getUser().getIdLong())) {
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
