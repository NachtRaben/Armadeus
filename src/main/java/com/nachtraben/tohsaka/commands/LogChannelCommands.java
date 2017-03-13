package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Cmd;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by NachtRaben on 3/12/2017.
 */
public class LogChannelCommands {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogChannelCommands.class);

	@Cmd(name = "logs", format = "<function> <type>", description = "Sets the logging channel for that message type")
	public void setLogChannel(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if (sender instanceof GuildCommandSender) {
			GuildCommandSender s = (GuildCommandSender) sender;
			if (s.getMember().isOwner() || s.getUser().getId().equals("118255810613608451")) {
				String function = args.get("function").toLowerCase();
				String type = args.get("type").toLowerCase();
				GuildManager manager = GuildManager.getManagerFor(s.getGuild());
				if (function.equals("set")) {
					switch (type) {
						case "generic":
							manager.getConfig().setGenericLogChannel(s.getChannel());
							s.getChannel().sendMessage("Generic logging channel set to `" + manager.getConfig().getGenericLogChannel().getName() + "`.").queue();
							break;
						case "music":
							manager.getConfig().setMusicLogChannel(s.getChannel());
							s.getChannel().sendMessage("Music logging channel set to `" + manager.getConfig().getMusicLogChannel().getName() + "`.").queue();
							break;
						case "admin":
							manager.getConfig().setAdminLogChannel(s.getChannel());
							s.getChannel().sendMessage("Admin logging channel set to `" + manager.getConfig().getAdminLogChannel().getName() + "`.").queue();
							break;
						default:
							s.getChannel().sendMessage("Invalid target type, valid targets are GENERIC, MUSIC, and ADMIN.").queue();
							break;
					}
				} else if (function.equals("clear")) {
					switch (type) {
						case "generic":
							manager.getConfig().setGenericLogChannel(null);
							break;
						case "music":
							manager.getConfig().setMusicLogChannel(null);
							break;
						case "admin":
							manager.getConfig().setAdminLogChannel(null);
							break;
						default:
							s.getChannel().sendMessage("Invalid target type, valid targets are GENERIC, MUSIC, and ADMIN.").queue();
							break;
					}
				} else if (function.equals("test")) {
					switch (type) {
						case "generic":
							MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), "Test of GENERIC types.");
							break;
						case "music":
							MessageUtils.sendMessage(MessageTargetType.MUSIC, s.getChannel(), "Test of MUSIC types.");
							break;
						case "admin":
							MessageUtils.sendMessage(MessageTargetType.ADMIN, s.getChannel(), "Test of ADMIN types.");
							break;
						case "all":
							MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), "Test of GENERIC types.");
							MessageUtils.sendMessage(MessageTargetType.MUSIC, s.getChannel(), "Test of MUSIC types.");
							MessageUtils.sendMessage(MessageTargetType.ADMIN, s.getChannel(), "Test of ADMIN types.");
							break;
						default:
							s.getChannel().sendMessage("Invalid target type, valid targets are GENERIC, MUSIC, ADMIN, or ALL.").queue();
							break;
					}
				} else if (function.equals("list") && type.equals("all")) {
					MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), "Generic: " +
							(manager.getConfig().getGenericLogChannel() != null ? manager.getConfig().getGenericLogChannel().getName() : "Not set."));
					MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), "Music: " +
							(manager.getConfig().getMusicLogChannel() != null ? manager.getConfig().getMusicLogChannel().getName() : "Not set."));
					MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), "Admin: " +
							(manager.getConfig().getAdminLogChannel() != null ? manager.getConfig().getAdminLogChannel().getName() : "Not set."));
				} else
					s.getChannel().sendMessage("Current functions for this command are: Set, Clear, Test").queue();
			}
		}
	}
}
