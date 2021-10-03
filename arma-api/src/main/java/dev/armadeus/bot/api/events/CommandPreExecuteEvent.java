package dev.armadeus.bot.api.events;

import co.aikar.commands.RootCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CommandPreExecuteEvent {

    private final DiscordCommandIssuer issuer;
    private final RootCommand command;
    private boolean allowed = true;

}
