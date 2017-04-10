package com.nachtraben.tohsaka.commands;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;

import java.util.Map;

/**
 * Created by NachtRaben on 3/12/2017.
 */
public class GuildOwnerCommands {

    @Cmd(name = "conf", format = "delete messages <boolean>", description = "Should the bot delete command messages.")
    public void deleteCommandMessages(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || sendee.getUser().getId().equals("118255810613608451")) {
                GuildConfig config = GuildManager.getManagerFor(sendee.getGuild()).getConfig();
                config.setDeleteCommandMessages(Boolean.parseBoolean(args.get("boolean")));
                MessageUtils.sendMessage(MessageTargetType.ADMIN, sendee.getChannel(), config.shouldDeleteCommandMessages() ? "Now deleting command messages." : "No longer deleting command messages.");
            }
        }
    }
}
