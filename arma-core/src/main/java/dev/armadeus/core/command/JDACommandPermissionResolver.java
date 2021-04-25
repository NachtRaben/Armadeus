package dev.armadeus.core.command;

import dev.armadeus.bot.api.ArmaCore;
import net.dv8tion.jda.api.Permission;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JDACommandPermissionResolver implements CommandPermissionResolver {
    private Map<String, Integer> discordPermissionOffsets;

    public JDACommandPermissionResolver() {
        discordPermissionOffsets = new HashMap<>();
        for (Permission permission : Permission.values()) {
            discordPermissionOffsets.put(permission.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "-"), permission.getOffset());
        }
    }

    @Override
    public boolean hasPermission(JDACommandManager manager, JDACommandEvent event, String permission) {
        // Explicitly return true if the issuer a developer. They are always allowed.
        if (ArmaCore.get().getArmaConfig().getDeveloperIds().contains(event.getIssuer().getAuthor().getIdLong())) {
            return true;
        }

        // Return false on webhook messages, as they cannot have permissions defined.
        if (event.getIssuer().isWebhookMessage()) {
            return false;
        }

        Integer permissionOffset = discordPermissionOffsets.get(permission);
        if (permissionOffset == null) {
            return false;
        }

        if(event.getIssuer().getMember() != null) {
            return event.getIssuer().getMember().hasPermission(Permission.getFromOffset(permissionOffset));
        } else {
            return false;
        }
    }
}
