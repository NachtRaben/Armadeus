package com.nachtraben.events;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by NachtRaben on 1/26/2017.
 */
public class PrivateMessageEvents extends ListenerAdapter {

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        boolean user = !event.getMessage().getAuthor().getId().equals(event.getJDA().getSelfUser().getId());
        Message message = event.getMessage();
        String content = message.getContent();
        if(!content.isEmpty());
            //LogManager.TOHSAKA.info((user ? "[%s#%s -> ME]: %s" : "[ME -> %s#%s]: %s"), event.getChannel().getUser().getName(),event.getChannel().getUser().getDiscriminator(), event.getMessage().getRawContent());
    }
    public void onPrivateMessageUpdate(PrivateMessageUpdateEvent event) {}
    public void onPrivateMessageDelete(PrivateMessageDeleteEvent event) {}
    public void onPrivateMessageEmbed(PrivateMessageEmbedEvent event) {}

}
