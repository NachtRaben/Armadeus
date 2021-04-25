package dev.armadeus.core.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandConfigProvider {
    CommandConfig provide(MessageReceivedEvent event);
}
