package dev.armadeus.core.command;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import dev.armadeus.core.ArmaCoreImpl;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NullCommandIssuer implements CommandIssuer {

    public static final NullCommandIssuer INSTANCE = new NullCommandIssuer();

    @Override
    public CommandIssuer getIssuer() {
        return this;
    }

    @Override
    public CommandManager getManager() {
        return ArmaCoreImpl.get().commandManager();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return new UUID(0, 0);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public void sendMessageInternal(String message) {
        // No-Op Implementation
    }
}
