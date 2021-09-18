package dev.armadeus.discord.welcome.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.electronwill.nightconfig.core.CommentedConfig;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.welcome.ArmaWelcome;

@CommandAlias("welcome")
@Conditions("guildonly")
@CommandPermission("administrator")
@Description("Used to configure the Arma-Welcomer for new guild members")
public class WelcomeCommand extends DiscordCommand {

    @Subcommand("set")
    @CommandPermission("administrator")
    public class WelcomeSetters extends BaseCommand {

        @Subcommand("enabled")
        @Description("Enables/Disables the welcome feature")
        public void setEnabled(DiscordCommandIssuer issuer, boolean enabled) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("enabled", enabled);
            issuer.sendMessage("Welcome is now `" + (enabled ? "enabled" : "disabled") + "`");
        }

        @Subcommand("dm")
        @Description("Set the welcome feature to DM the user on join")
        public void setDm(DiscordCommandIssuer issuer, boolean dm) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("dm", dm);
            issuer.sendMessage("Welcome DMs are now `" + (dm ? "enabled" : "disabled") + "`");
        }

        @Subcommand("message")
        @Description("Set a message to send when a user joins your guild")
        public void setDm(DiscordCommandIssuer issuer, String message) {
            CommentedConfig config = ArmaWelcome.get().getConfig(issuer.getGuild());
            config.set("message", message);
            issuer.sendMessage("Welcome message is now `" + message + "`");
        }
    }

}
