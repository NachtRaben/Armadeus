package com.nachtraben.core.listeners;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.command.PrivateCommandSender;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class DiscordCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCommandListener.class);

    private DiscordBot bot;

    public DiscordCommandListener(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getRawContent();
        List<User> mentions = message.getMentionedUsers();

        if(!message.getAuthor().isBot() && !message.getAuthor().isFake() && content.length() > 0) {
            DiscordCommandSender sender = null;
            String prefix = null;

            if(!mentions.isEmpty() && mentions.get(0).equals(message.getJDA().getSelfUser()))
                prefix = mentions.get(0).getAsMention();

            if(message.isFromType(ChannelType.TEXT)) {
                sender = new GuildCommandSender(bot, message);
            } else if(message.isFromType(ChannelType.PRIVATE)) {
                sender = new PrivateCommandSender(bot, message);
            } else {
                LOGGER.warn("Received message from unsupported channel type { " + message.getChannelType() + " }.");
            }

            if(prefix != null) {
                for(String pref : bot.getConfig().getDefaultPrefixes()) {
                    prefix = pref;
                    break;
                }
            }

            if(bot.isDebugging() &&
                    !bot.getConfig().getOwnerIDs().contains(message.getAuthor().getIdLong()) &&
                    !bot.getConfig().getDeveloperIDs().contains(message.getAuthor().getIdLong()))
                return;

            if(prefix != null && sender != null) {
                String[] tokens = content.replaceFirst(prefix, "").split("\\s+");
                String command = tokens[0];
                String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                try {
                    sender.runCommand(command, args);
                } catch(Exception e) {
                    LOGGER.error("Failed to run command.", e);
                }
            }

        }
    }
}
