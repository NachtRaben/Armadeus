package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.orangeslice.CommandBase;

public class DiscordCommandBase extends CommandBase {

    private DiscordBot bot;

    public DiscordCommandBase(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void registerCommands(Object object) {
        super.registerCommands(object);
    }
}
