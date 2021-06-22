package dev.armadeus.core.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Private;
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

    @CommandAlias("publish")
    public void importSlashCommands(DiscordCommandIssuer user) {
        JDA shard = user.getJda();
        Guild guild = shard.getGuildById(317784247949590528L);
        if (guild == null)
            return;
        user.sendMessage("Publishing commands...");
        SlashCommandsUtil util = new SlashCommandsUtil(ArmaCoreImpl.get());
        Collection<CommandData> generated = util.generateCommandData();
        CommandListUpdateAction commandsUpdate = guild.updateCommands();
        commandsUpdate.addCommands(generated).queue(success -> user.sendMessage("Published %s commands", (Object)generated.size()));
    }

}
