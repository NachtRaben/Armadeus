package com.nachtraben.commandrework;

/**
 * Created by NachtRaben on 2/4/2017.
 */
public class CommandResult {

    Command command;
    String[] args;
    String[] flags;

    String message;

    Throwable stack;

    Result result;

    public CommandResult() {
        result = Result.SUCCESS;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getFlags() {
        return flags;
    }

    public void setFlags(String[] flags) {
        this.flags = flags;
    }

    public void setArgsAndFlags(String[] args, String[] flags) {
        this.args = args;
        this.flags = flags;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getStack() {
        return stack;
    }

    public void setStack(Throwable stack) {
        this.stack = stack;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public boolean succeeded() {
        return result.equals(Result.SUCCESS);
    }

    public enum Result {
        SUCCESS,
        FAILURE,
        INVALID_ARGS,
        INVALID_FLAGS,
        UNKNOWN_COMMAND,
        EXCEPTION
    }
}
