package dev.armadeus.core.listeners;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.DiscordBot;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class DiscordCommandListener extends ListenerAdapter {
    private static final Logger log = LogManager.getLogger();

    private final DiscordBot bot;

    public DiscordCommandListener(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Armadeus.getInstance().getPings().put(event.getAuthor().getIdLong(), System.nanoTime());
        Message message = event.getMessage();
        String content = message.getContentRaw();
        JDA jda = message.getJDA();

        if (bot.isDevMode() && message.getAuthor().getIdLong() != 118255810613608451L) return;

        if (!message.getAuthor().isBot() && !content.isEmpty()) {
            if (bot.isLogMessages() && message.isFromType(ChannelType.TEXT)) {
                log.debug(String.format("[Message][%s>>%s#%s]: %s", message.getGuild().getName(), message.getAuthor().getName(), message.getAuthor().getDiscriminator(), message.getContentDisplay()));
            } else if (bot.isLogMessages() && message.isFromType(ChannelType.PRIVATE)) {
                log.debug(String.format("[Message][DM>>%s#%s]: %s", message.getAuthor().getName(), message.getAuthor().getDiscriminator(), message.getContentDisplay()));
            }

//            DiscordCommandSender sender = null;
//            String prefix = null;
//            try {
//                // Check global bot mention
//                String botMention = "^<@(!)?(" + event.getJDA().getSelfUser().getIdLong() + ")>.*";
//                if (content.matches(botMention)) {
//                    prefix = botMention + " ";
//                }
//
//                if (message.isFromGuild()) {
//                    sender = new GuildCommandSender(bot, message);
//                    // Check prefix again
//                    if (prefix == null) {
//                        // Check Member Mention
//                        GuildConfig config = ((GuildCommandSender) sender).getGuildConfig();
//                        for (String pref : config.getPrefixes()) {
//                            if (content.startsWith(pref)) {
//                                prefix = pref;
//                                break;
//                            }
//                        }
//                    }
//                } else if (!message.isFromGuild()) {
//                    sender = new PrivateCommandSender(bot, message);
//                    if (prefix == null) {
//                        for (String pref : BotConfig.get().getGlobalPrefixes()) {
//                            if (content.startsWith(pref)) {
//                                prefix = pref;
//                                break;
//                            }
//                        }
//                    }
//                } else {
//                    log.warn("Received message from unsupported currentChannel type { " + message.getChannelType() + " }.");
//                }
//
//                if (prefix != null && sender != null) {
//                    String[] tokens = content.replaceFirst(prefix, "").split("\\s+");
//                    String command = tokens[0];
//                    String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
//                    try {
//                        sender.runCommand(command, args).get();
//                    } catch (Exception e) {
//                        sender.sendMessage(ChannelTarget.GENERIC, "I was unable to process your command, please try again later.");
//                        log.error("An exception occurred while attempting to run a command.", e);
//                    }
//                }
//
//            } catch (Exception e) {
//                if (sender != null)
//                    sender.sendMessage(ChannelTarget.GENERIC, "I was unable to process your command, please try again later.");
//                log.error("An exception occurred while trying to query the database.", e);
//            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        TextChannel channel = bot.getShardManager().getTextChannelById(357952462960721920L);
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
        TextChannel channel = bot.getShardManager().getTextChannelById(357952462960721920L);
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
