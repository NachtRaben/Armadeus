package com.nachtraben.core.utils;

import com.nachtraben.core.managers.GuildManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by NachtRaben on 3/11/2017.
 */
public class MessageUtils {

	private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

	public static void sendMessage(MessageTargetType type, TextChannel channel, String message) {
		TextChannel target = GuildManager.getManagerFor(channel.getGuild()).getRecommendedChannelFor(type);
		if(target == null) {
			target = channel;
		}
		try {
			target.sendMessage(message).queue();
		} catch (Exception e) {
			logger.warn("Failed to send message, " + message + ", to " + channel.getName() + " in " + channel.getGuild().getName() + ".", e);
		}
	}

	public static void sendMessage(MessageTargetType type, TextChannel channel, Message message) {
		TextChannel target = GuildManager.getManagerFor(channel.getGuild()).getRecommendedChannelFor(type);
		if(target == null) target = channel;
		try {
			target.sendMessage(message).queue();
		} catch (Exception e) {
			logger.warn("Failed to send message, " + message + ", to " + channel.getName() + " in " + channel.getGuild().getName() + ".", e);
		}
	}

	public static void sendMessage(MessageTargetType type, TextChannel channel, MessageEmbed message) {
		TextChannel target = GuildManager.getManagerFor(channel.getGuild()).getRecommendedChannelFor(type);
		if(target == null) target = channel;
		try {
			target.sendMessage(message).queue();
		} catch (Exception e) {
			if(e instanceof PermissionException)
				logger.warn("Failed to send message, " + message + ", to " + channel.getName() + " in " + channel.getGuild().getName() + ". " + e.getMessage());
			else
				logger.warn("Failed to send message, " + message + ", to " + channel.getName() + " in " + channel.getGuild().getName() + ".", e);
		}
	}
}
