package com.nachtraben.core.utils;

import com.nachtraben.core.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by NachtRaben on 2/6/2017.
 */
public class ConsoleCommandImpl implements Runnable {

    public static ConsoleCommandImpl instance;

    static {
        // This will make it so that there may only be 1 ConsoleCommandImplementation.
        instance = new ConsoleCommandImpl();
    }

    private Thread thread;
    private Scanner scanner;

    private ConsoleCommandImpl() {
        // Init the thread.
        this.thread = new Thread(this);
        // Init the system input scanner.
        scanner = new Scanner(System.in);
    }

    public void start() {
        // Start the thread if it isn't currently running.
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    public void stop() {
        // TODO: Check if you have a BotInstance.running and return if it isn't running.
        try {
            thread.join(1000);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void run() {
        String message;
        // It would be better to change this to like BotInstance.running or something of the sorts.
        while (thread.isAlive()) {
            if ((message = scanner.nextLine()) != null) {
                String[] tokens = StringUtils.splitAtSpaces(message);
                String command = tokens[0];
                String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                ConsoleCommandSender.getInstance().runCommand(command, args);
            }
        }
    }
}
