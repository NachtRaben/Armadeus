package dev.armadeus.core.plugin;

public class ArmaCorePlugin {

    private static ArmaCorePlugin instance;

    public ArmaCorePlugin() {
        instance = this;
    }

    public static ArmaCorePlugin get() {
        return instance;
    }
}
