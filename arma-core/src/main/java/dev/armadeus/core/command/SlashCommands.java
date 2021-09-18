package dev.armadeus.core.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.core.ArmaCoreImpl;
import dev.armadeus.core.util.SlashCommandsUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Collection;
import java.util.List;

@Private
@Conditions("owneronly")
@CommandAlias("slash")
public class SlashCommands extends DiscordCommand {

    @Subcommand("destroy")
    @Description("Destroys currently published slash commands")
    public void destroySlashCommands(DiscordCommandIssuer user) {
        user.sendMessage("Destroying commands...");
        user.getJda().updateCommands().queue(s -> user.sendMessage("Purged all Global commands"));
        user.getGuild().updateCommands().queue(s -> user.sendMessage("Purged all Guild commands"));
    }

    @Subcommand("publish")
    @Description("Publishes all commands to discord as slash commands")
    public void importSlashCommands(DiscordCommandIssuer user) {
        SlashCommandsUtil util = new SlashCommandsUtil((ArmaCoreImpl) core);
        Collection<CommandData> generated = util.generateCommandData();
        user.getJda().updateCommands().addCommands(generated).queue(
                s -> user.sendMessage("Published %s commands globally", String.valueOf(generated.size())),
                f -> {
                    user.sendMessage("Failed to publish CommandData, %s", f.getMessage());
                    logger.error("Failed to publish CommandData", f);
                });
    }

}
