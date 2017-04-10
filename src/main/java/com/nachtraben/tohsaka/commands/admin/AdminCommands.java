package com.nachtraben.tohsaka.commands.admin;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.JDABot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
            StringBuilder sb = new StringBuilder();
            sb.append("WebSocket: `" + sendee.getJDA().getPing() + "`ms.\n");
            sendee.getChannel().sendTyping().complete();
            sb.append(String.format("API Ping `%s`ms", System.currentTimeMillis() - start));
            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), sb.toString());
        }
    }

    @Cmd(name = "clean", format = "", description = "Cleans my messages and messages from provided mentions from the chat.")
    public void clean(CommandSender sender) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            List<Message> messages = sendee.getChannel().getHistory().retrievePast(100).complete().stream()
                    .filter(message -> message.getAuthor().equals(sendee.getJDA().getSelfUser())
                            && !message.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                    .collect(Collectors.toList());
            try {
                if(messages.size() > 2) {
                    sendee.getChannel().deleteMessages(messages).queue();
                } else if(messages.size() == 1) {
                    messages.get(0).delete().queue();
                }
            } catch (PermissionException e) {
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "I was unable to delete my messages! I am missing the required permissions.");
            }
        }
    }

    @Cmd(name = "clean", format = "{mentions}", description = "Cleans my messages from the chat.")
    public void cleanMentions(CommandSender sender) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(sendee.getMember().hasPermission(Permission.MESSAGE_MANAGE) || sendee.getUser().getId().equals("118255810613608451")) {
                List<User> mentions = sendee.getMessage().getMentionedUsers();
                List<Message> messages = sendee.getChannel().getHistory().retrievePast(100).complete().stream()
                        .filter(message ->
                                (message.getAuthor().equals(sendee.getJDA().getSelfUser()) || mentions.contains(message.getAuthor()))
                                        && !message.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                        .collect(Collectors.toList());
                try {
                    if(messages.size() > 2) {
                        sendee.getChannel().deleteMessages(messages).queue();
                    } else if(messages.size() == 1) {
                        messages.get(0).delete().queue();
                    }
                } catch(PermissionException e) {
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "I was unable to delete my messages! I am missing the required permissions.");
                }
            } else {
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You do not have the required perms to delete user messages.");
            }
        }
    }

    @Cmd(name = "about", format = "", description = "Give information about the bot.")
    public void about(CommandSender sender) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            EmbedBuilder builder = new EmbedBuilder();
            GuildConfig config = GuildManager.getManagerFor(sendee.getGuild()).getConfig();
            ApplicationInfo info = sendee.getJDA().asBot().getApplicationInfo().complete();
            builder.setAuthor("Tohsaka", "https://tohsaka.nachtraben.com", sendee.getJDA().getSelfUser().getAvatarUrl());
            builder.setThumbnail(info.getIconUrl());
            builder.setTitle("About Tohsaka:", null);
            builder.setDescription(info.getDescription());
            builder.setFooter(String.format("Author: %s#%s", info.getOwner().getName(), info.getOwner().getDiscriminator()), info.getOwner().getAvatarUrl());
            builder.addField("Prefixes", config.getGuildPrefixes().isEmpty() ? JDABot.getInstance().getDefaultCommandPrefixes().toString() : config.getGuildPrefixes().toString(), false);
            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), builder.build());
        }
    }

    @Cmd(name = "disconnect", format = "", description = "Closes my audio connection.")
    public void disconnect(CommandSender sender) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(sendee.getGuild().getAudioManager().isConnected())
                sendee.getGuild().getAudioManager().closeAudioConnection();
        }
    }
}
