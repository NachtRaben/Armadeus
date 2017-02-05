package com.nachtraben.events;

import com.nachtraben.Tohsaka;
import com.nachtraben.command.CmdBase;
import com.nachtraben.command.sender.UserCommandSender;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class GuildMessageEvents extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();
        if(content != null && content.length() > 0) {
            if(!message.getAuthor().isBot() && CmdBase.COMMAND_INIT_CHARS.contains(content.charAt(0))) {
                String s = content.substring(1);
                String[] tokens = s.split(" ");
                String command = tokens[0];
                String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[] {};
                Tohsaka.commandHandler.process(new UserCommandSender(message), command, args);
                if(!event.getJDA().getStatus().equals(JDA.Status.SHUTTING_DOWN));
                    //message.deleteMessage().queue();
            } else {
                // TODO: Not command
            }
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {

    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {

    }

    @Override
    public void onGuildMessageEmbed(GuildMessageEmbedEvent event) {

    }

}
