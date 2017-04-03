package com.nachtraben.tohsaka.commands.admin;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by NachtRaben on 4/2/2017.
 */
public class ConfCommands {

	@Cmd(name = "conf", format = "conf set prefixes (prefixes)", description = "Sets the command prefixes for this guild")
	public void setPrefixes(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
			if(sender instanceof GuildCommandSender) {
				GuildCommandSender sendee = (GuildCommandSender) sender;
				if(verifySender(sendee)) {
					String prefixes = args.get("prefixes");
					String[] tokens = null;
					if(prefixes != null) {
						tokens = prefixes.split("\\s+");
						tokens = Arrays.stream(tokens).filter(s -> (!s.isEmpty())).toArray(String[]::new);
					}
					GuildManager manager = GuildManager.getManagerFor(sendee.getGuild());
					manager.getConfig().setCommandPrefixes(tokens);
					if(manager.getConfig().getGuildPrefixes() != null) {
						MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "The prefixes for this guild are now `" + manager.getConfig().getGuildPrefixes().toString() + "`.");
					} else {
						MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "The prefixes for this guild are now `" + JDABot.getInstance().getDefaultCommandPrefixes() + "`.");
					}
				} else {
					MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You do not have the Administrative permissions required for this command.");
				}
			}
	}

	private boolean verifySender(GuildCommandSender sendee) {
		if(sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || sendee.getUser().getId().equals("118255810613608451"))
			return true;
		return false;
	}


}
