package co.aikar.commands;

import com.velocitypowered.api.event.Subscribe;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class JDAListener {

    private final JDACommandManager manager;

    JDAListener(JDACommandManager manager) {
        this.manager = manager;
    }

    @Subscribe
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            this.manager.dispatchEvent(event);
        }
    }

    @Subscribe
    public void onSlashCommand(SlashCommandEvent event) {
        this.manager.dispatchSlash(event);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        manager.initializeBotOwner();
    }
}
