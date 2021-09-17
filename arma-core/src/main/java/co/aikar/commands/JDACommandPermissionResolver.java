package co.aikar.commands;

import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.events.PermissionCheckEvent;
import dev.armadeus.core.ArmaCoreImpl;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
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
        // Explicitly return true if the issuer is a developer. They are always allowed.
        if (ArmaCoreImpl.get().armaConfig().getDeveloperIds().contains(event.getUser().getIdLong())) {
            return true;
        }

        // Return false on webhook messages, as they cannot have permissions defined.
        if (!event.isSlashEvent() && event.getIssuer().isWebhookMessage()) {
            return false;
        }

        // If it's not from a guild
        if (event.getGuild() == null) {
            return true;
        }

        // If we don't have member objects
        if (event.getMember() == null) {
            return false;
        }

        // Check vanilla discord perms
        Integer permissionOffset = discordPermissionOffsets.get(permission);
        if (permissionOffset != null && !event.getMember().hasPermission(Permission.getFromOffset(permissionOffset))) {
            return false;
        }

        // If it isn't a discord perm, check guild configuration
        GuildConfig config = ArmaCoreImpl.get().guildManager().getConfigFor(event.getGuild());
        List<Long> roles = event.getMember().getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
        Set<String> blocked = new HashSet<>(config.getDisabledCommands());
        for (Long role : roles) {
            blocked.addAll(config.getDisabledCommandsForRole(role));
        }
        for (String b : blocked) {
            if (permission.matches(b)) {
                return false;
            }
        }

        try {
            return ArmaCoreImpl.get().eventManager().fire(new PermissionCheckEvent(event.getUser())).get().isAllowed();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception encountered in permissions check event", e);
            return false;
        }
    }
}
