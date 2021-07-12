package dev.armadeus.discord.commands.core;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collections;
import java.util.Set;

@CommandAlias("config|conf")
@Conditions("guildonly")
@CommandPermission("administrator")
public class ConfCommands extends DiscordCommand {

    @Subcommand("set prefixes")
    public void setPrefixes(DiscordCommandIssuer user, @Default() String[] prefixes) {
        GuildConfig config = user.getGuildConfig();
        if (prefixes.length != 0) {
            config.setPrefixes(Set.of(prefixes));
            user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
        } else {
            config.setPrefixes(Collections.emptySet());
            user.sendMessage("The guild prefixes have been cleared falling back to `" + core.armaConfig().getDeveloperIds() + "`");
        }
    }

    @Subcommand("set cooldown")
    public void setCommandCooldown(DiscordCommandIssuer user, int cooldown) {
        cooldown = Math.max(-1, cooldown);
        GuildConfig config = user.getGuildConfig();
        config.setCommandCooldown(cooldown);
        if (cooldown <= 0)
            user.sendMessage("The guild command cooldown has been disabled");
        else
            user.sendMessage("The guild command cooldown has been set to `" + cooldown + "` seconds");
    }

    @Subcommand("set purgedelay")
    public void setPurgeDelay(DiscordCommandIssuer user, int delay) {
        delay = Math.max(-1, delay);
        GuildConfig config = user.getGuildConfig();
        config.setPurgeDelay(delay);
        if (delay == -1)
            user.sendMessage("The bot response purge delay is now disabled");
        else if (!config.deleteCommandMessages())
            user.sendMessage("The bot response delay has been set to `" + delay + "`, but is configured not to delete user commands, purge delay will be ignored for conformity");
        else if (delay == 0)
            user.sendMessage("The bot response delay has been set back to default `" + DiscordCommandIssuer.defaultPurgeDelay + "` seconds");
        else
            user.sendMessage("The bot response purge delay has been set to `" + delay + "` seconds");
    }

    @Subcommand("delete commands")
    public void deleteUserCommands(DiscordCommandIssuer user, boolean delete) {
        GuildConfig config = user.getGuildConfig();
        config.deleteCommandMessages(delete);
        if (delete)
            user.sendMessage("The bot will now delete user command messages");
        else
            user.sendMessage("The bot will no longer delete user command messages");
    }

    @Subcommand("add prefix")
    public void addPrefix(DiscordCommandIssuer user, String prefix) {
        GuildConfig config = user.getGuildConfig();
        config.addPrefixes(prefix);
        user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
    }

    @Subcommand("remove prefix")
    public void removePrefix(DiscordCommandIssuer user, String prefix) {
        GuildConfig config = user.getGuildConfig();
        config.removePrefixes(prefix);
        user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
    }

    @Subcommand("command blacklist")
    public void blacklistPerm(DiscordCommandIssuer issuer, String permission, @Optional Role role) {
        if (role != null) {
            issuer.getGuildConfig().addDisabledCommandsForRole(role.getIdLong(), permission);
            issuer.sendMessage("Disabled command `%s` for `%s`".formatted(permission, role.getName()));
        } else {
            issuer.getGuildConfig().addDisabledCommand(permission);
            issuer.sendMessage("Disabled command `%s` for `@everyone`".formatted(permission));
        }
    }

    @Subcommand("commands whitelist")
    public void whitelistPerm(DiscordCommandIssuer issuer, String permission, @Optional Role role) {
        if (role != null) {
            issuer.getGuildConfig().removeDisabledCommandsForRole(role.getIdLong(), permission);
            issuer.sendMessage("Enabled command `%s` for `%s`".formatted(permission, role.getName()));
        } else {
            issuer.getGuildConfig().removeDisabledCommand(permission);
            issuer.sendMessage("Enabled command `%s` for `@everyone`".formatted(permission));
        }
    }
}
