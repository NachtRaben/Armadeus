package dev.armadeus.discord.welcome.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import com.electronwill.nightconfig.core.CommentedConfig;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.welcome.ArmaWelcome;

@CommandAlias("welcome")
@Conditions("guildonly")
@CommandPermission("administrator")
public class WelcomeCommand extends DiscordCommand {

    @Subcommand("set")
    @CommandPermission("administrator")
    public class WelcomeSetters extends BaseCommand {

        @Subcommand("enabled")
        public void setEnabled(DiscordCommandIssuer issuer, boolean enabled) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("enabled", enabled);
            issuer.sendMessage("Welcome is now `" + (enabled ? "enabled" : "disabled") + "`");
        }

        @Subcommand("dm")
        public void setDm(DiscordCommandIssuer issuer, boolean dm) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("dm", dm);
            issuer.sendMessage("Welcome DMs are now `" + (dm ? "enabled" : "disabled") + "`");
        }

        @Subcommand("message")
        public void setDm(DiscordCommandIssuer issuer, String message) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("message", message);
            issuer.sendMessage("Welcome message is now `" + message + "`");
        }
    }

}
