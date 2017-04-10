package com.nachtraben.core.utils;

import com.nachtraben.core.managers.GuildManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by NachtRaben on 3/11/2017.
 */
public class MessageUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    // TODO: NSFW message filtering.

    public static void sendMessage(MessageTargetType type, TextChannel channel, String message) {
        sendMessage(type, channel, new MessageBuilder().append(message).build());
    }

    public static void sendMessage(MessageTargetType type, TextChannel channel, Message message) {
        if(channel == null) throw new IllegalArgumentException("Provided channel was null!");
        GuildManager manager = GuildManager.getManagerFor(channel.getGuild());
        TextChannel target = manager.getRecommendedChannelFor(type);
        if(target == null)
            target = channel;

        try {
            target.sendMessage(message).queue();
        } catch (Exception e) {
            logger.warn("Failed to send message, " + message + ", to " + channel.getName() + " in " + channel.getGuild().getName() + ".", e.getMessage());
        }
    }

    public static void sendMessage(MessageTargetType type, TextChannel channel, MessageEmbed message) {
        sendMessage(type, channel, new MessageBuilder().setEmbed(message).build());
    }
}
