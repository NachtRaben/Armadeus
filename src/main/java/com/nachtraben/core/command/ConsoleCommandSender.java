package com.nachtraben.core.command;

import com.nachtraben.core.JDABot;
import com.nachtraben.core.commandmodule.CommandEvent;
import com.nachtraben.core.commandmodule.CommandSender;

import java.util.Scanner;
import java.util.concurrent.Future;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public class ConsoleCommandSender implements CommandSender {
    private static ConsoleCommandSender instance;

    private Scanner input;

    private ConsoleCommandSender() {
        instance = this;
        input = new Scanner(System.in);
    }

    public static ConsoleCommandSender getInstance() {
        if(instance == null) instance = new ConsoleCommandSender();
        return instance;
    }

    @Override
    public void sendMessage(String s) {
        System.out.println(s);
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
        return JDABot.getInstance().getCommandHandler().execute(this, command, args);
    }
}
