package dev.armadeus.core.command;

public interface CommandPermissionResolver {
    boolean hasPermission(JDACommandManager manager, JDACommandEvent event, String permission);
}
