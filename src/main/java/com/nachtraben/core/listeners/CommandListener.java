package com.nachtraben.core.listeners;

import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class CommandListener extends ListenerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);
	public static CommandListener instance;

	static {
		instance = new CommandListener();
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String content = event.getMessage().getRawContent();
		List<User> mentions = event.getMessage().getMentionedUsers();

		if (!event.getAuthor().isBot() && content != null && content.length() > 0) {
			GuildCommandSender sender = new GuildCommandSender(event.getMessage());
			GuildManager guildManager = GuildManager.getManagerFor(event.getGuild().getId());
			String prefix = null;

			if (!mentions.isEmpty() && mentions.get(0).equals(event.getJDA().getSelfUser())) {
				prefix = mentions.get(0).getAsMention() + " ";
			} else if (!Tohsaka.debug && guildManager.getConfig().getGuildPrefixes() != null && !guildManager.getConfig().getGuildPrefixes().isEmpty()) {
				for (String s : guildManager.getConfig().getGuildPrefixes()) {
					if (content.startsWith(s)) {
						prefix = s;
						break;
					}
				}
			} else {
				for (String s : JDABot.getInstance().getDefaultCommandPrefixes()) {
					if (content.startsWith(s)) {
						prefix = s;
						break;
					}
				}
			}

			//TODO Check guild prefixes first

			if (Tohsaka.debug && !event.getAuthor().getId().equals("118255810613608451"))
				return;

			if (prefix != null) {
				String message = content.replaceFirst(prefix, "");
				String[] tokens = message.split("\\s+");
				String command = tokens[0];
				String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
				try {
					sender.runCommand(command, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			LOGGER.info(String.format("[%s#%s][%s#%s] %s", event.getGuild().getName(), event.getGuild().getId(), event.getMember().getEffectiveName(), event.getAuthor().getId(), content));
		}
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		// TODO: Implement this
	}

}
