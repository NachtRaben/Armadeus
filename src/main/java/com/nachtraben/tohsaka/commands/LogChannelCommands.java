package com.nachtraben.tohsaka.commands;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by NachtRaben on 3/12/2017.
 */
public class LogChannelCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogChannelCommands.class);

    @Cmd(name = "conf", format = "logs <function> <type>", description = "Sets the logging channel for that message type")
    public void setLogChannel(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if (sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || sendee.getUser().getId().equals("118255810613608451")) {
                String function = args.get("function").toLowerCase();
                String type = args.get("type").toLowerCase();
                GuildManager manager = GuildManager.getManagerFor(sendee.getGuild());
                if (function.equals("set")) {
                    switch (type) {
                        case "generic":
                            manager.getConfig().setGenericLogChannel(sendee.getChannel());
                            sendee.getChannel().sendMessage("Generic logging channel set to `" + manager.getConfig().getGenericLogChannel().getName() + "`.").queue();
                            break;
                        case "music":
                            manager.getConfig().setMusicLogChannel(sendee.getChannel());
                            sendee.getChannel().sendMessage("Music logging channel set to `" + manager.getConfig().getMusicLogChannel().getName() + "`.").queue();
                            break;
                        case "admin":
                            manager.getConfig().setAdminLogChannel(sendee.getChannel());
                            sendee.getChannel().sendMessage("Admin logging channel set to `" + manager.getConfig().getAdminLogChannel().getName() + "`.").queue();
                            break;
                        default:
                            sendee.getChannel().sendMessage("Invalid target type, valid targets are GENERIC, MUSIC, and ADMIN.").queue();
                            break;
                    }
                } else if (function.equals("clear")) {
                    switch (type) {
                        case "generic":
                            manager.getConfig().setGenericLogChannel(null);
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "GENERIC logging channel cleared.");
                            break;
                        case "music":
                            manager.getConfig().setMusicLogChannel(null);
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "MUSIC logging channel cleared.");
                            break;
                        case "admin":
                            manager.getConfig().setAdminLogChannel(null);
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "ADMIN logging channel cleared.");
                            break;
                        default:
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(),"Invalid target type, valid targets are GENERIC, MUSIC, and ADMIN.");
                            break;
                    }
                } else if (function.equals("test")) {
                    switch (type) {
                        case "generic":
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Test of GENERIC types.");
                            break;
                        case "music":
                            MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Test of MUSIC types.");
                            break;
                        case "admin":
                            MessageUtils.sendMessage(MessageTargetType.ADMIN, sendee.getChannel(), "Test of ADMIN types.");
                            break;
                        case "all":
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Test of GENERIC types.");
                            MessageUtils.sendMessage(MessageTargetType.MUSIC, sendee.getChannel(), "Test of MUSIC types.");
                            MessageUtils.sendMessage(MessageTargetType.ADMIN, sendee.getChannel(), "Test of ADMIN types.");
                            break;
                        default:
                            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(),"Invalid target type, valid targets are GENERIC, MUSIC, and ADMIN.");
                            break;
                    }
                } else if (function.equals("list") && type.equals("all")) {
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Generic: " +
                            (manager.getConfig().getGenericLogChannel() != null ? manager.getConfig().getGenericLogChannel().getName() : "Not set."));
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Music: " +
                            (manager.getConfig().getMusicLogChannel() != null ? manager.getConfig().getMusicLogChannel().getName() : "Not set."));
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Admin: " +
                            (manager.getConfig().getAdminLogChannel() != null ? manager.getConfig().getAdminLogChannel().getName() : "Not set."));
                } else
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Current functions for this command are: Set, Clear, Test");
            } else {
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You do not have the Administrative permissions required to run this command.");
            }
        }
    }
}
