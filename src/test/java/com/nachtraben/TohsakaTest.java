package com.nachtraben;

import com.nachtraben.command.CmdBase;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.commandrework.CommandBase;
import com.nachtraben.commandrework.CommandResult;
import com.nachtraben.log.LogManager;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 2/1/2017.
 */
public class TohsakaTest implements Runnable {

    Tohsaka tohsaka;
    private Thread console;

    public static CommandBase commandHandler;

    public Scanner input = new Scanner(System.in);

    public TohsakaTest() {
        //tohsaka = new Tohsaka(1, true);
        commandHandler = new CommandBase();
        commandHandler.registerCommands(new TestCommands());
        console = new Thread(this);
        console.start();
    }

    public static void main(String... args) {
        new TohsakaTest();
    }

    @Override
    public void run() {
        while(true) {
            String line = input.nextLine();
            if (line != null && !line.isEmpty()) {
                if (CmdBase.COMMAND_INIT_CHARS.contains(line.charAt(0))) {
                    String s = line.substring(1);
                    String[] tokens = s.split(" ");
                    String command = tokens[0];
                    String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                    long start = System.nanoTime();
                    CommandResult result = commandHandler.process(ConsoleCommandSender.getInstance(), command, args);
                    long end = System.nanoTime();
                    LogManager.TOHSAKA.debug(format("%s took %sns to finish entire execution. %sms.", command.toUpperCase(), end-start, TimeUnit.NANOSECONDS.toMillis(end-start)));
                    if(!result.succeeded() && !result.getResult().equals(CommandResult.Result.UNKNOWN_COMMAND)) {
                        if(result.getResult().equals(CommandResult.Result.EXCEPTION))
                            LogManager.TOHSAKA.error(format("Failed to execute command { %s } due to a { %s }.", command, result.getStack().getClass().getSimpleName()), result.getStack());
                        else
                            LogManager.TOHSAKA.error(format("Failed to execute command { %s } due to { %s } and message { %s }. ", command, result.getResult().toString(), result.getMessage() != null ? result.getMessage() : "Unprovided"));

                    }
                } else {
                    // Not a command
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogManager.TOHSAKA.warn("Console command thread failed to sleep quietly.");
            }
        }
    }
}
