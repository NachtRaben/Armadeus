package com.nachtraben.core.listeners;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.command.PrivateCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.configuration.RedisBotConfig;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
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
        JDA jda = message.getJDA();

        if (!message.getAuthor().isBot() && !message.getAuthor().isFake() && content.length() > 0) {
            DiscordCommandSender sender = null;
            String prefix = null;
            try {
                if (bot.isDebugging() &&
                        !bot.getConfig().getOwnerIDs().contains(message.getAuthor().getIdLong()) &&
                        !bot.getConfig().getDeveloperIDs().contains(message.getAuthor().getIdLong()))
                    return;

                if (!mentions.isEmpty() && mentions.get(0).equals(jda.getSelfUser()) && content.startsWith(mentions.get(0).getAsMention())) {
                    LOGGER.debug("User mention.");
                    if(bot.getConfig() instanceof RedisBotConfig && !bot.isDebugging() && ((RedisBotConfig) bot.getConfig()).isDebugging()) {
                        LOGGER.warn("Ignoring user mention prefix as a developer instance is running.");
                    } else {
                        prefix = mentions.get(0).getAsMention() + " ";
                    }
                }

                if (message.isFromType(ChannelType.TEXT)) {
                    sender = new GuildCommandSender(bot, message);
                    if(prefix == null) {
                        Member botMember = message.getGuild().getMember(jda.getSelfUser());
                        if(!mentions.isEmpty() && mentions.get(0).equals(jda.getSelfUser()) && content.startsWith(botMember.getAsMention())) {
                            LOGGER.debug("Member mention.");
                            if(bot.getConfig() instanceof RedisBotConfig && !bot.isDebugging() && ((RedisBotConfig) bot.getConfig()).isDebugging()) {
                                LOGGER.warn("Ignoring member mention prefix as a developer instance is running.");
                            } else {
                                prefix = botMember.getAsMention() + " ";
                            }
                        }
                    }
                    GuildConfig config = bot.getGuildManager().getConfigurationFor(message.getGuild().getIdLong());
                    if (!bot.isDebugging() && prefix == null && !config.getPrefixes().isEmpty()) {
                        for (String pref : config.getPrefixes()) {
                            if (content.startsWith(pref)) {
                                prefix = pref;
                                break;
                            }
                        }
                    }
                } else if (message.isFromType(ChannelType.PRIVATE)) {
                    sender = new PrivateCommandSender(bot, message);
                } else {
                    LOGGER.warn("Received message from unsupported currentChannel type { " + message.getChannelType() + " }.");
                }

                if (!bot.isDebugging() && prefix == null) {
                    for (String pref : bot.getConfig().getPrefixes()) {
                        if (content.startsWith(pref)) {
                            prefix = pref;
                            break;
                        }
                    }
                }

                if (prefix != null && sender != null) {
                    String[] tokens = content.replaceFirst(prefix, "").split("\\s+");
                    String command = tokens[0];
                    String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                    try {
                        sender.runCommand(command, args);
                    } catch (Exception e) {
                        LOGGER.error("Failed to run command.", e);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("An exception occurred while trying to query the database.", e);
            }


        }
    }
}
