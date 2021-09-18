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
        JDA shard = user.getJda();
        Guild guild = shard.getGuildById(317784247949590528L);
        if (guild == null)
            return;
        user.sendMessage("Destroying commands...");
//        guild.retrieveCommands().queue(c -> {
//            c.forEach(cc -> {
//                guild.deleteCommandById(cc.getIdLong()).queue();
//            });
//        });
        guild.updateCommands().queue();
//        shard.retrieveCommands().queue(c -> {
//            c.forEach(cc -> shard.deleteCommandById(cc.getIdLong()).queue());
//        });
    }

    @Subcommand("publish")
    @Description("Publishes all commands to discord as slash commands")
    public void importSlashCommands(DiscordCommandIssuer user) {
        JDA shard = user.getJda();
        Guild guild = shard.getGuildById(317784247949590528L);
        if (guild == null)
            return;
        user.sendMessage("Generating command metadata...");
        SlashCommandsUtil util = new SlashCommandsUtil((ArmaCoreImpl) core);
        Collection<CommandData> generated = util.generateCommandData();
        guild.updateCommands().addCommands(generated).queue(
                s -> user.sendMessage("Generated metadata for %s commands", String.valueOf(generated.size())),
                f -> {
                    user.sendMessage("Failed to publish command data, %s", f.getMessage());
                    logger.error("Failed to update guild commands", f);
                });
    }

}
