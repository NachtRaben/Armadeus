package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;

public class PrivateCommandSender extends DiscordCommandSender {

    private PrivateChannel privateChannel;

    public PrivateCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        if(!message.isFromType(ChannelType.PRIVATE))
            throw new IllegalArgumentException("Message must be from type " + ChannelType.PRIVATE + ".");
        privateChannel = message.getPrivateChannel();
    }

    public PrivateChannel getPrivateChannel() {
        if(privateChannel == null) privateChannel = getUser().openPrivateChannel().complete();
        return privateChannel;
    }

    @Override
    public void sendMessage(String message) {
        getPrivateChannel().sendMessage(message).queue();
    }
}
