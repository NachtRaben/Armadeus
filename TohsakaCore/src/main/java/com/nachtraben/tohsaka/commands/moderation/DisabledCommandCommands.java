package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.command.SubCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisabledCommandCommands extends CommandTree {

    public DisabledCommandCommands() {
        getChildren().add(new SubCommand("conf", "disable <command> for {roles}", "Adds the role(s) to the disabled command.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = sendee.getDbot().getConfig();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if(!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIDs().contains(sendee.getUserID()) && !botConfig.getDeveloperIDs().contains(sendee.getUserID())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you don't have permission to use that command.");
                        return;
                    }
                    String command = args.get("command");
                    String[] roles = args.get("roles").split("\\s+");
                    List<Command> commands = sendee.getDbot().getCommandBase().getCommand(command);
                    if(commands == null || commands.isEmpty()) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I couldn't find that command.");
                        return;
                    }
                    HashSet<Long> disRoles = new HashSet<>();
                    for(Role r : sendee.getMessage().getMentionedRoles())
                        disRoles.add(r.getIdLong());
                    for(String s : roles) {
                        if(s.equalsIgnoreCase("@everyone")) {
                            disRoles.add(-1L);
                        } else if(!s.startsWith("<") && !s.endsWith(">")) {
                            try {
                                Long l = Long.parseLong(s);
                                disRoles.add(l);
                            } catch (NumberFormatException e) {
                                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but `" + s + "` is not a valid role ID.");
                                return;
                            }
                        }
                    }
                    for(Long l : disRoles) {
                        guildConfig.addDisabledCommand(commands.get(0).getName(), l);
                    }
                    guildConfig.save();
                    sendee.sendMessage(ChannelTarget.GENERIC, "Disabled `" + commands.get(0).getName() + "` for `" + disRoles.size() + "` roles.");
                }
            }
        });
        getChildren().add(new SubCommand("conf", "enable <command> for {roles}", "Adds the role(s) to the disabled command.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = sendee.getDbot().getConfig();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if(!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIDs().contains(sendee.getUserID()) && !botConfig.getDeveloperIDs().contains(sendee.getUserID())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you don't have permission to use that command.");
                        return;
                    }
                    String command = args.get("command");
                    String[] roles = args.get("roles").split("\\s+");
                    List<Command> commands = sendee.getDbot().getCommandBase().getCommand(command);
                    if(commands == null || commands.isEmpty()) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I couldn't find that command.");
                        return;
                    }
                    HashSet<Long> disRoles = new HashSet<>();
                    for(Role r : sendee.getMessage().getMentionedRoles())
                        disRoles.add(r.getIdLong());
                    for(String s : roles) {
                        if(s.equalsIgnoreCase("@everyone")) {
                            disRoles.add(-1L);
                        } else if(!s.startsWith("<") && !s.endsWith(">")) {
                            try {
                                Long l = Long.parseLong(s);
                                disRoles.add(l);
                            } catch (NumberFormatException e) {
                                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but `" + s + "` is not a valid role ID.");
                                return;
                            }
                        }
                    }

                    Map<String, Set<Long>> disabledRoles = guildConfig.getDisabledCommands();
                    if(!disabledRoles.containsKey(commands.get(0).getName())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "There are no roles added for that command.");
                        return;
                    }

                    for(Long l : disRoles) {
                        guildConfig.removeDisabledCommand(commands.get(0).getName(), l);
                    }
                    guildConfig.save();
                    sendee.sendMessage(ChannelTarget.GENERIC, "Enabled `" + commands.get(0).getName() + "` for `" + disRoles.size() + "` roles.");
                }
            }
        });
        getChildren().add(new SubCommand("conf", "get roles for <command>", "Gets the role(s) on the disabled command.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if (sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig botConfig = sendee.getDbot().getConfig();
                    GuildConfig guildConfig = sendee.getGuildConfig();
                    if (!sendee.getMember().isOwner() && !sendee.getMember().hasPermission(Permission.ADMINISTRATOR) && !botConfig.getOwnerIDs().contains(sendee.getUserID()) && !botConfig.getDeveloperIDs().contains(sendee.getUserID())) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you don't have permission to use that command.");
                        return;
                    }
                    Map<String, Set<Long>> commands = guildConfig.getDisabledCommands();
                    if(!commands.containsKey(args.get("command"))) {
                        sendee.sendMessage(ChannelTarget.GENERIC, "There are no " + (guildConfig.isBlacklist() ? "blacklisted " : "whitelisted ") + " roles for `" + args.get("command") + "`.");
                    } else {
                        sendee.sendMessage(ChannelTarget.GENERIC, "The following roles are " + (guildConfig.isBlacklist() ? "blacklisted " : "whitelisted ") + " for `" + args.get("command") + "`.\n`" + commands.get(args.get("command")) + "`");
                    }
                }
            }
        });
    }

}
