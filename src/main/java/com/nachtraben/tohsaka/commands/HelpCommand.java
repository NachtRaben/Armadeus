package com.nachtraben.tohsaka.commands;

import com.nachtraben.commandapi.Command;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.utils.HasteBin;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by NachtRaben on 3/17/2017.
 */
public class HelpCommand extends Command {


	public HelpCommand() {
		super("help", "[command]");
	}

	@Override
	public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			String command = args.get("command");
			List<Command> commands;
			if(command != null) {
				commands = JDABot.getInstance().getCommandHandler().getCommands().get(command);
			} else {
				commands = new ArrayList<>();
				for(Map.Entry<String, List<Command>> entry : JDABot.getInstance().getCommandHandler().getCommands().entrySet()) {
					commands.addAll(entry.getValue());
				}
			}
			if(commands != null) {
				StringBuilder sb = new StringBuilder();
				for(Command c : commands) {
					sb.append(c.helpString()).append("\n");
				}
				if(sb.length() < 1750) {
					MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), new EmbedBuilder().setTitle("Help for, " +  (command != null ? command : "ALL") + ".", null).appendDescription(sb.toString()).build());
				} else {
					MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Here are the commands, too long for a message. " + new HasteBin(sb.toString().replace("**", "")).getHaste() + ".txt");
				}
			}
		}
	}
}
