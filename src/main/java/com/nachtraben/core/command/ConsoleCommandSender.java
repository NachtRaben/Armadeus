package com.nachtraben.core.command;

import com.nachtraben.orangeslice.CommandEvent;
import com.nachtraben.orangeslice.CommandSender;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Future;

public class ConsoleCommandSender implements CommandSender, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleCommandSender.class);

    private static ConsoleCommandSender instance;
    private Thread runnable;
    private Scanner input;

    public ConsoleCommandSender() {
        Asserts.check(instance == null, "There is already an instance of the ConsoleCommandSender");
        instance = this;
        input = new Scanner(System.in);
        runnable = new Thread(this);
        runnable.run();
    }

    @Override
    public void sendMessage(String s) {
        LOGGER.info("MESSAGE: " + s);
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public Future<CommandEvent> runCommand(String command, String[] args) {
        //return Tohsaka.getInstance().getCmdBase().execute(this, command, args);
        return null;

    }

    @Override
    public void run() {
        String message;
        while(runnable.isAlive()) {
            while((message = input.nextLine()) != null) {
                String[] tokens = message.split("\\s+");
                String command = tokens[0];
                String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                runCommand(command, args);
            }
        }
    }

}
