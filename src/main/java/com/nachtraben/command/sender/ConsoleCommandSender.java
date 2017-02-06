package com.nachtraben.command.sender;

import com.nachtraben.Tohsaka;
import com.nachtraben.command.CmdBase;
import com.nachtraben.command.PermissionLevel;
import com.nachtraben.log.LogManager;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public class ConsoleCommandSender implements CommandSender, Runnable {

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
        LogManager.TOHSAKA.info("[MESSAGE] " + s);
    }

    @Override
    public boolean hasPermission(PermissionLevel p) {
        return true;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public void run() {
        String line = null;
        while(Tohsaka.instance.running) {
            line = input.nextLine();
            if(line != null && !line.isEmpty()) {
                if(CmdBase.COMMAND_INIT_CHARS.contains(line.charAt(0))) {
                    String s = line.substring(1);
                    String[] tokens = s.split(" ");
                    String command = tokens[0];
                    String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[] {};
                    Tohsaka.commandHandler.process(new ConsoleCommandSender(), command, args);
                } else {
                    // TODO: Not command
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
