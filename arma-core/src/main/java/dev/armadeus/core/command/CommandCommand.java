package dev.armadeus.core.command;

import co.aikar.commands.ACFPatterns;
import co.aikar.commands.JDACommandExecutionContext;
import co.aikar.commands.JDARootCommand;
import co.aikar.commands.RegisteredCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.core.ArmaCoreImpl;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Arrays;
import java.util.Locale;

@CommandPermission("administrator")
@CommandAlias("command|cmd")
public class CommandCommand extends DiscordCommand {

    @CommandAlias("lookup")
    public void lookup(DiscordCommandIssuer user, String[] args) {
        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        JDARootCommand root = (JDARootCommand) ArmaCoreImpl.get().commandManager().getRootCommand(args[0]);
        if(root == null) {
            user.sendMessage("Unable to find an exact command from those search parameters");
            return;
        }

        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        RegisteredCommand<JDACommandExecutionContext> rcmd = root.findSubCommand(cmd, args);
        if(rcmd == null) {
            user.sendMessage("Unable to find an exact command from those search parameters");
            return;
        }

        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        builder.setTitle("Properties of " + root.getCommandName());
        Conditions conditions = rcmd.getAnnotation(Conditions.class);
        CommandAlias aliases = rcmd.getAnnotation(CommandAlias.class);
        if(!rcmd.getRequiredPermissions().isEmpty())
            builder.addField("permissions", String.join("\n", rcmd.getRequiredPermissions()), true);
        if(conditions != null && !conditions.value().isEmpty())
            builder.addField("conditions", String.join("\n", conditions.value().split(ACFPatterns.PIPE.pattern())), true);
        if(aliases != null && !aliases.value().isEmpty())
            builder.addField("aliases", String.join("\n", aliases.value().split(ACFPatterns.PIPE.pattern())), false);
        user.sendMessage(builder.build());
    }
}
