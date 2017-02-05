package com.nachtraben.command.sender;

import com.nachtraben.command.PermissionLevel;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public interface CommandSender {

    void sendMessage(String s);
    boolean hasPermission(PermissionLevel p);
    String getName();

}
