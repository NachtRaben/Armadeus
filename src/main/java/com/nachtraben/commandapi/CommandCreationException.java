package com.nachtraben.commandapi;

/**
 * Created by NachtRaben on 3/8/2017.
 */
public class CommandCreationException extends RuntimeException {

    private Command command;
    private String message;

    public CommandCreationException(Command command, String message) {
        super(message);
        this.command = command;
        this.message = message;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
