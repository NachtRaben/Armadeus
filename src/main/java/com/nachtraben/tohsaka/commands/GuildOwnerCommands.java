package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Cmd;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;

import java.util.Map;

/**
 * Created by NachtRaben on 3/12/2017.
 */
public class GuildOwnerCommands {

	@Cmd(name = "go", format = "delete messages <boolean>", description = "Should the bot delete command messages.")
	public void deleteCommandMessages(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender s = (GuildCommandSender) sender;
			if(s.getMember().isOwner() || s.getUser().getId().equals("118255810613608451")) {
				GuildConfig config = GuildManager.getManagerFor(s.getGuild()).getConfig();
				config.setDeleteCommandMessages(Boolean.parseBoolean(args.get("boolean")));
				MessageUtils.sendMessage(MessageTargetType.ADMIN, s.getChannel(), config.shouldDeleteCommandMessages() ? "Now deleting command messages." : "No longer deleting command messages.");
			}
		}
	}

}
