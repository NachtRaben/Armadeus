package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandResult;
import com.nachtraben.orangeslice.CommandSender;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.util.concurrent.Future;

public class DiscordCommandSender implements CommandSender {

    private DiscordBot dbot;

    private JDA jda;
    private User user;
    private Message message;
    private MessageChannel messageChannel;

    private long userID;
    private long messageID;
    private long messageChannelID;

    public DiscordCommandSender(DiscordBot dbot, Message message) {
        this.dbot = dbot;
        this.jda = message.getJDA();
        this.user = message.getAuthor();
        this.message = message;
        this.messageChannel = message.getChannel();

        this.userID = user.getIdLong();
        this.messageID = message.getIdLong();
        this.messageChannelID = messageChannel.getIdLong();
    }

    public DiscordBot getDbot() {
        return dbot;
    }

    public User getUser() {
        if(user == null) user = dbot.getShardManager().getUserByID(userID);
        return user;
    }

    public Message getMessage() {
        if(message == null) message = getMessageChannel().getMessageById(messageID).complete();
        return message;
    }

    public MessageChannel getMessageChannel() {
        if(messageChannel == null) messageChannel = dbot.getShardManager().getTextChannelByID(messageChannelID);
        if(messageChannel == null) messageChannel = dbot.getShardManager().getPrivateChannelByID(messageChannelID);
        return messageChannel;
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
        messageChannel.sendMessage(message).queue();
    }

    public void sendMessage(MessageEmbed embed) {
        messageChannel.sendMessage(embed).queue();
    }

    public void sendMessage(Message message) {
        messageChannel.sendMessage(message).queue();
    }

    public boolean hasPermission() {
        return true;
    }

    @Override
    public String getName() {
        return user.getName();
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