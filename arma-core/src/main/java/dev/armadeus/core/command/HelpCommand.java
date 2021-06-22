package dev.armadeus.core.command;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.JDARootCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;

public class HelpCommand extends DiscordCommand {

//    @CommandAlias("help")
//    public void help(DiscordCommandIssuer user) {
//        // TODO: Why the fuck does this need casting...
//        for (JDARootCommand cmd : (Collection<JDARootCommand>) core.commandManager().getRegisteredRootCommands()) {
//            CommandHelp help = cmd.getCommandHelp(user, new String[0]);
//            help.showHelp();
//        }
//    }

    @CommandAlias("help")
    public void showHelp(DiscordCommandIssuer user, String command, @Default String[] args) {
        JDARootCommand rootCommand = (JDARootCommand) core.commandManager().getRootCommand(command);
        if (rootCommand == null) {
            return;
        }
        CommandHelp help = rootCommand.getCommandHelp(user, args);
        help.showHelp();
    }
}
