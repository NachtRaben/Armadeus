package com.nachtraben.command.sender;

import com.nachtraben.command.PermissionLevel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public class UserCommandSender implements CommandSender {

    Message commandMessage;

    public UserCommandSender(Message message) {
        this.commandMessage = message;
    }

    @Override
    public void sendMessage(String s) {
        PrivateChannel channel = commandMessage.getAuthor().getPrivateChannel();
        channel.sendMessage(s).queue();
    }

    public Message getCommandMessage() {
        return commandMessage;
    }

    @Override
    public boolean hasPermission(PermissionLevel p) {
        return p.has(commandMessage.getAuthor(), commandMessage.getGuild());
    }

    @Override
    public String getName() {
        return commandMessage.getAuthor().getName();
    }
}
