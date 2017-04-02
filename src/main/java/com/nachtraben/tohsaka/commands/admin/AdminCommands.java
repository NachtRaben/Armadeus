package com.nachtraben.tohsaka.commands.admin;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.nachtraben.core.utils.StringUtils;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.Permission;

import java.util.Map;


/**
 * Created by NachtRaben on 1/18/2017.
 */
public class AdminCommands {

    @Cmd(name = "invite", format = "", description = "Generates the invite link for the bot.")
    public void invite(CommandSender sender, Map<String, String> args) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			MessageUtils.sendMessage(MessageTargetType.GENERIC,
					sendee.getChannel(),
					String.format("Here is my invite link! https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s", sendee.getMessage().getJDA().getSelfUser().getId(), "8"));
		}
    }

    @Cmd(name = "shutdown", format = "", description = "Shuts down Tohsaka.")
    public void onShutdown(CommandSender sender, Map<String, String> args) {
    	if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			if(sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || sendee.getUser().getId().equals("118255810613608451"))
				Tohsaka.getInstance().shutdown();
			else
				MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You do not have the Administrative permissions required for this command.");
		} else {
    		Tohsaka.getInstance().shutdown();
		}
    }
    
    @Cmd(name = "ping", format = "", description = "Gives you the ping of the bot.")
    public void ping(CommandSender sender) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender sendee = (GuildCommandSender) sender;
			Long start = System.currentTimeMillis();
			sendee.getChannel().sendTyping().queue(aVoid -> MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), StringUtils.format("Pong: `%s`ms.", System.currentTimeMillis() - start)));
		}
    }

}
