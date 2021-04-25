package dev.armadeus.core.command;

import co.aikar.commands.MessageFormatter;

public class JDAMessageFormatter extends MessageFormatter<String> {
    public JDAMessageFormatter() {
        // JDA does not support coloring messages outside of embed fields.
        // We pass three empty strings so as to remove color coded messages from appearing.
        super("", "", "");
    }

    @Override
    public String format(String color, String message) {
        return message;
    }
}
