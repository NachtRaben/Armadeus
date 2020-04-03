package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandResult;
import com.nachtraben.orangeslice.CommandSender;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Future;

public class DiscordCommandSender implements CommandSender, Serializable {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(DiscordCommandSender.class);

    private final transient DiscordBot dbot;

    private final SnowflakeReference<User> user;
    private final SnowflakeReference<Message> message;
    private SnowflakeReference<MessageChannel> channel;

    public DiscordCommandSender(DiscordBot dbot, Message message) {
        this.dbot = dbot;
        this.user = new SnowflakeReference<>(message.getAuthor(), id -> dbot.getShardManager().getUserById(id));
        this.message = new SnowflakeReference<>(message, id -> channel.resolve().getHistory().getMessageById(id));
        this.channel = new SnowflakeReference<>(message.getChannel(), id -> {
            switch (message.getChannelType()) {
                case TEXT:
                    return dbot.getShardManager().getTextChannelById(id);
                case PRIVATE:
                    return user.resolve().openPrivateChannel().complete();
            }
            return null;
        });
    }

    public DiscordBot getDbot() {
        return dbot;
    }

    public long getUserId() {
        return user.getIdLong();
    }

    public User getUser() {
        return user.resolve();
    }

    public long getMessageId() {
        return message.getIdLong();
    }

    public Message getMessage() {
        return message.resolve();
    }

    public long getMessageChannelId() {
        return channel.getIdLong();
    }

    public MessageChannel getMessageChannel() {
        return channel.resolve();
    }

    public void sendPrivateMessage(String message) {
        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    public void sendPrivateMessage(MessageEmbed embed) {
        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(embed).queue());
    }

    public void sendPrivateMessage(Message message) {
        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    @Override
    public void sendMessage(String message) {
        MessageChannel channel = getMessageChannel();
        if (channel != null)
            channel.sendMessage(message).queue();
    }

    public void sendMessage(MessageEmbed embed) {
        MessageChannel channel = getMessageChannel();
        if (channel != null)
            channel.sendMessage(embed).queue();
    }

    public void sendMessage(Message message) {
        MessageChannel channel = getMessageChannel();
        if (channel != null)
            channel.sendMessage(message).queue();
    }

    public boolean hasPermission() {
        return true;
    }

    @Override
    public String getName() {
        User user = getUser();
        if(user != null) {
            return user.getName();
        } else {
            return "UNKNOWN_USER";
        }
    }

    @Override
    public Future<CommandResult> runCommand(String command, String[] args) {
        return dbot.getCommandBase().execute(this, command, args);
    }

    public void sendMessage(ChannelTarget target, String message) {
        sendMessage(message);
    }

    public void sendMessage(ChannelTarget target, Message message) {
        sendMessage(message);
    }

    public void sendMessage(ChannelTarget target, MessageEmbed embed) {
        sendMessage(embed);
    }

}
