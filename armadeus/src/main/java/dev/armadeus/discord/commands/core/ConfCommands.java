package dev.armadeus.discord.commands.core;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;

import java.util.Arrays;
import java.util.Collections;

@CommandAlias("config|conf")
public class ConfCommands extends DiscordCommand {

    @Subcommand("set")
    @CommandPermission("administrator")
    public class ConfSetters extends BaseCommand {

        @Subcommand("prefixes")
        public void setPrefixes(DiscordCommandIssuer user, @Default() String[] prefixes) {
            GuildConfig config = user.getGuildConfig();
            if (prefixes.length != 0) {
                config.setPrefixes(Arrays.asList(prefixes));
                user.sendMessage("The guild prefixes have been updated to `" + config.getPrefixes() + "`");
            } else {
                config.setPrefixes(Collections.emptyList());
                user.sendMessage("The guild prefixes have been cleared falling back to `" + core.armaConfig().getDeveloperIds() + "`");
            }
        }

        @Subcommand("cooldown")
        public void setCommandCooldown(DiscordCommandIssuer user, int cooldown) {
            cooldown = Math.max(-1, cooldown);
            GuildConfig config = user.getGuildConfig();
            config.setCommandCooldown(cooldown);
            if (cooldown <= 0)
                user.sendMessage("The guild command cooldown has been disabled");
            else
                user.sendMessage("The guild command cooldown has been set to `" + cooldown + "` seconds");
        }

        @Subcommand("purgedelay")
        public void setPurgeDelay(DiscordCommandIssuer user, int delay) {
            delay = Math.max(-1, delay);
            GuildConfig config = user.getGuildConfig();
            config.setPurgeDelay(delay);
            if (delay == -1)
                user.sendMessage("The bot response purge delay is now disabled");
            else if(!config.deleteCommandMessages())
                user.sendMessage("The bot response delay has been set to `" + delay + "`, but is configured not to delete user commands, purge delay will be ignored for conformity");
            else if (delay == 0)
                user.sendMessage("The bot response delay has been set back to default `" + DiscordCommandIssuer.defaultPurgeDelay + "` seconds");
            else
                user.sendMessage("The bot response purge delay has been set to `" + delay + "` seconds");
        }

        @Subcommand("deletecommands")
        public void deleteUserCommands(DiscordCommandIssuer user, boolean delete) {
            GuildConfig config = user.getGuildConfig();
            config.setDeleteCommandMessages(delete);
            if(delete)
                user.sendMessage("The bot will now delete user command messages");
            else
                user.sendMessage("The bot will no longer delete user command messages");
        }
    }
}
