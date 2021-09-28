package dev.armadeus.core.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collections;
import java.util.Set;

@CommandAlias("config|conf")
@Conditions("guildonly")
@CommandPermission("administrator")
@Description("Configuration commands to control bot behavior")
public class ConfCommands extends DiscordCommand {

    @Subcommand("set prefixes")
    @Description("Set prefixes the bot will respond too in your guild")
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
    @Description("UNIMPLEMENTED, Set the command cooldown for your guild")
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
    @Description("Set how many seconds bot messages will linger in your guild")
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
    @Description("Set if the bot will delete command messages as they are processed")
    public void deleteUserCommands(DiscordCommandIssuer user, boolean delete) {
        GuildConfig config = user.getGuildConfig();
        config.deleteCommandMessages(delete);
        if (delete)
            user.sendMessage("The bot will now delete user command messages");
        else
            user.sendMessage("The bot will no longer delete user command messages");
    }

    @Subcommand("add prefix")
    @Description("Add a prefix the bot will respond to in your guild")
    public void addPrefix(DiscordCommandIssuer user, String prefix) {
        GuildConfig config = user.getGuildConfig();
        config.addPrefixes(prefix);
        user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
    }

    @Subcommand("remove prefix")
    @Description("Remove a prefix that the bot will respond to in your guild")
    public void removePrefix(DiscordCommandIssuer user, String prefix) {
        GuildConfig config = user.getGuildConfig();
        config.removePrefixes(prefix);
        user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
    }

    @Subcommand("command blacklist")
    @Description("Blacklist a command from execution in your guild")
    public void blacklistPerm(DiscordCommandIssuer issuer, String permission, @Optional Role role) {
        if (role != null) {
            issuer.getGuildConfig().addDisabledCommandsForRole(role.getIdLong(), permission);
            issuer.sendMessage(String.format("Disabled command `%s` for `%s`", permission, role.getName()));
        } else {
            issuer.getGuildConfig().addDisabledCommand(permission);
            issuer.sendMessage(String.format("Disabled command `%s` for `@everyone`", permission));
        }
    }

    @Subcommand("command whitelist")
    @Description("Whitelist a command for execution in your guild")
    public void whitelistPerm(DiscordCommandIssuer issuer, String permission, @Optional Role role) {
        if (role != null) {
            issuer.getGuildConfig().removeDisabledCommandsForRole(role.getIdLong(), permission);
            issuer.sendMessage(String.format("Enabled command `%s` for `%s`", permission, role.getName()));
        } else {
            issuer.getGuildConfig().removeDisabledCommand(permission);
            issuer.sendMessage(String.format("Enabled command `%s` for `@everyone`", permission));
        }
    }

    @Subcommand("show blacklist")
    @Description("Show disabled commands for your guild")
    public void showBlacklist(DiscordCommandIssuer issuer, @Optional Role role) {
        Set<String> disabledCommands = role != null ? issuer.getGuildConfig().getDisabledCommandsForRole(role.getIdLong()) : issuer.getGuildConfig().getDisabledCommands();

        if (disabledCommands == null || disabledCommands.isEmpty()) {
            issuer.sendMessage("There are no disabled commands available!");
            return;
        }

        EmbedBuilder builder = EmbedUtils.newBuilder(issuer).setTitle("Blacklisted Commands" + (role != null ? (" for " + role.getAsMention()) : ""));
        for (String c : disabledCommands) {
            builder.appendDescription(c + "\n");
        }
        issuer.sendMessage(builder.build());
    }
}

