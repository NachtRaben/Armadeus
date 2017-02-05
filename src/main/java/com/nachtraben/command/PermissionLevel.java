package com.nachtraben.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

// Written by Xilixir on 2016-08-29
public enum PermissionLevel {
    USER,
    MOD,
    OWNER;

    PermissionLevel() {
    }

    public boolean has(User user, Guild guild){
       return true;
    }
}
