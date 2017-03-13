package com.nachtraben.core.commandmodule;

import java.util.concurrent.Future;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public interface CommandSender {

    void sendMessage(String s);
    boolean hasPermission();
    String getName();

    Future<CommandEvent> runCommand(String command, String[] args);
}
