package dev.armadeus.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandConfig;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.JDACommandEvent;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.JDARootCommand;
import co.aikar.commands.RegisteredCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.bot.api.util.StringUtils;
import dev.armadeus.core.ArmaCoreImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends DiscordCommand {

    @CommandAlias("help")
    public void help(DiscordCommandIssuer user) {
        // TODO: Why the fuck does this need casting...
        //noinspection unchecked
        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        builder.setTitle("Commands");
        Collection<JDARootCommand> commands = (Collection<JDARootCommand>) core.commandManager().getRegisteredRootCommands();
        commands = commands.stream().sorted(Comparator.comparing(JDARootCommand::getCommandName)).collect(Collectors.toList());
        List<String> descriptions = new ArrayList<>();
        for (JDARootCommand cmd : commands) {
            if(!cmd.hasAnyPermission(user) || cmd.getSubCommands().values().stream().anyMatch(RegisteredCommand::isPrivate))
                continue;

            CommandHelp help = cmd.getCommandHelp(user, new String[0]);
            if(cmd.getDescription() == null || cmd.getDescription().isEmpty() || descriptions.contains(cmd.getDescription())) {
                continue;
            }
            descriptions.add(cmd.getDescription());
            builder.appendDescription(String.format("`%s` - %s\n", help.getCommandName(), cmd.getDescription()));
        }
        String prefix = "/";
        if(user.getIssuer() instanceof MessageReceivedEvent && user.isFromGuild()) {
            CommandConfig config = ArmaCoreImpl.get().commandManager().getCommandConfig(user.getIssuer());
            for (String pf : config.getCommandPrefixes()) {
                if (((MessageReceivedEvent) user.getIssuer()).getMessage().getContentRaw().startsWith(pf)) {
                    prefix = pf;
                    break;
                }
            }
        }
        builder.setFooter("To see more specific help, use " + prefix + "help <command>");
        user.sendMessage(builder.build());
    }

    @CommandAlias("help")
    @Description("Shows help and usage information for the specified command")
    public void showHelp(DiscordCommandIssuer user, String command, @Default String[] args) {
        JDARootCommand rootCommand = (JDARootCommand) core.commandManager().getRootCommand(command);
        if (rootCommand == null) {
            return;
        }
        CommandHelp help = rootCommand.getCommandHelp(user, args);
        help.showHelp();
    }
}
