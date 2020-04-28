package com.nachtraben.core.listeners;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.command.PrivateCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;

public class DiscordCommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(DiscordCommandListener.class);

    private final DiscordBot dbot;

    public DiscordCommandListener(DiscordBot dbot) {
        this.dbot = dbot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        JDA jda = message.getJDA();

        if (!message.getAuthor().isBot() && !message.getAuthor().isFake() && content.length() > 0) {

            if (dbot.isLogMessages() && message.isFromType(ChannelType.TEXT)) {
                log.debug(String.format("[Message][%s>>%s#%s]: %s", message.getGuild().getName(), message.getAuthor().getName(), message.getAuthor().getDiscriminator(), message.getContentDisplay()));
            } else if (dbot.isLogMessages() && message.isFromType(ChannelType.PRIVATE)) {
                log.debug(String.format("[Message][DM>>%s#%s]: %s", message.getAuthor().getName(), message.getAuthor().getDiscriminator(), message.getContentDisplay()));
            }

            DiscordCommandSender sender = null;
            String prefix = null;
            try {
                // TODO: Re-implement debugging prefixes
                // Check bot mention
                String botMention = event.getJDA().getSelfUser().getAsMention();
                if (content.startsWith(botMention)) {
                    prefix = botMention + " ";
                }

                if (message.isFromType(ChannelType.TEXT)) {
                    // Check prefix again
                    if (prefix == null) {
                        String memberMention = event.getGuild().getSelfMember().getAsMention();
                        if (content.startsWith(memberMention)) {
                            // Check member mention
                            prefix = memberMention + " ";
                        } else {
                            GuildConfig config = ((GuildCommandSender) sender).getGuildConfig();
                            // Check guild prefixes
                            for (String pref : config.getPrefixes()) {
                                if (content.startsWith(pref)) {
                                    prefix = pref;
                                    break;
                                }
                            }
                        }
                        // Initialize sender instance
                        sender = new GuildCommandSender(dbot, message);
                    }
                } else if (message.isFromType(ChannelType.PRIVATE)) {
                    sender = new PrivateCommandSender(dbot, message);
                } else {
                    log.warn("Received message from unsupported currentChannel type { " + message.getChannelType() + " }.");
                }

                // Check global prefixes
                if (prefix == null) {
                    // Don't check global prefixes if we checked all the guild prefixes
                    if (sender instanceof GuildCommandSender && !((GuildCommandSender) sender).getGuildConfig().getPrefixes().isEmpty())
                        return;

                    for (String pref : dbot.getConfig().getPrefixes()) {
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
                        sender.runCommand(command, args).get();
                    } catch (Exception e) {
                        sender.sendMessage(ChannelTarget.GENERIC, "I was unable to process your command, please try again later.");
                        log.error("An exception occurred while attempting to run a command.", e);
                    }
                }

            } catch (Exception e) {
                if (sender != null)
                    sender.sendMessage(ChannelTarget.GENERIC, "I was unable to process your command, please try again later.");
                log.error("An exception occurred while trying to query the database.", e);
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        TextChannel channel = dbot.getShardManager().getTextChannelById(357952462960721920L);
        if (channel != null) {
            Guild g = event.getGuild();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Joined a new guild!");
            eb.setDescription("__**" + g.getName() + "#" + g.getIdLong() + "**__");
            eb.setThumbnail(event.getGuild().getIconUrl());
            eb.addField("Owner:", g.getOwner().getUser().getName() + "#" + g.getOwner().getUser().getDiscriminator(), true);
            eb.addField("Members:", "+" + g.getMembers().size(), true);
            eb.addField("TextChannels:", "+" + g.getTextChannels().size(), true);
            eb.addField("VoiceChannels:", "+" + g.getVoiceChannels().size(), true);
            eb.addField("Emotes:", "+" + g.getEmotes().size(), true);
            eb.setColor(Color.GREEN);
            channel.sendMessage(eb.build()).queue();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        TextChannel channel = dbot.getShardManager().getTextChannelById(357952462960721920L);
        if (channel != null) {
            Guild g = event.getGuild();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Left a guild!");
            eb.setDescription("__**" + g.getName() + "#" + g.getIdLong() + "**__");
            eb.setThumbnail(event.getGuild().getIconUrl());
            eb.addField("Owner:", g.getOwner().getUser().getName() + "#" + g.getOwner().getUser().getDiscriminator(), true);
            eb.addField("Members:", "-" + g.getMembers().size(), true);
            eb.addField("TextChannels:", "-" + g.getTextChannels().size(), true);
            eb.addField("VoiceChannels:", "-" + g.getVoiceChannels().size(), true);
            eb.addField("Emotes:", "-" + g.getEmotes().size(), true);
            eb.setColor(Color.RED);
            channel.sendMessage(eb.build()).queue();
        }
    }
}
