package com.nachtraben.commandapi;

/**
 * Created by NachtRaben on 3/7/2017.
 */
public class CommandEvent {

    private CommandSender sender;
    private Command command;
    private Result result;
    private Throwable throwable;

    public CommandEvent(CommandSender sender, Command command, Result result) {
        this(sender, command, result, null);
    }

    public CommandEvent(CommandSender sender, Command command, Result result, Throwable throwable) {
        this.sender = sender;
        this.command = command;
        this.result = result;
        this.throwable = throwable;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Command getCommand() {
        return command;
    }

    public Result getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "CommandEvent(" +
                "sender=" + sender.getName() + "#" + sender.getClass().getSimpleName() +
                ", command=" + (command != null ? command.toString() : "NULL") +
                ", result=" + result.toString() +
                ", throwable=" + (throwable != null ? throwable.getMessage() : "NULL") +
                ")";
    }

    public enum Result {
        SUCCESS, COMMAND_NOT_FOUND, EXCEPTION, INVALID_FLAGS
    }
}


