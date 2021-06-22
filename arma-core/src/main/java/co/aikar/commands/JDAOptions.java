package co.aikar.commands;

import org.jetbrains.annotations.NotNull;

public class JDAOptions {
    CommandConfig defaultConfig = new JDACommandConfig();
    CommandConfigProvider configProvider = null;
    CommandPermissionResolver permissionResolver = new JDACommandPermissionResolver();

    public JDAOptions() {
    }

    public JDAOptions defaultConfig(@NotNull CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    public JDAOptions configProvider(@NotNull CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
        return this;
    }

    public JDAOptions permissionResolver(@NotNull CommandPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
        return this;
    }

}
