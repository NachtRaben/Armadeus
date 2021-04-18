package dev.armadeus.bot.commands;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.Utils;
import dev.armadeus.command.CommandBase;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;
import dev.armadeus.command.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommands {

    private static final int RESULTS_LIMIT = 10;

    @Cmd(name = "help", format = "[command/page]", description = "Provides help information.")
    public void help(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;

            int page = 1;
            List<Command> commands = null;
            CommandBase base = Armadeus.getInstance().getCommandBase();

            String var = args.get("command/page");

            if (var != null) {
                commands = base.getCommand(var);
                if (commands == null) {
                    try {
                        page = Integer.parseInt(var);
                        if (page <= 0) {
                            page = 1;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (commands == null) {
                commands = new ArrayList<>();
                for (List<Command> cmds : Armadeus.getInstance().getCommandBase().getCommands().values()) {
                    commands.addAll(cmds);
                }
            }

            if(((page - 1) * RESULTS_LIMIT) > commands.size()) {
                sendee.sendMessage(ChannelTarget.GENERIC, "There are no commands on page `" + page + "`.");
                return;
            }

            if (commands.isEmpty() && var != null) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but no commands were found for `" + var + "`.");
                return;
            }

            int start = RESULTS_LIMIT * (page - 1);
            int end = RESULTS_LIMIT * page;
            boolean hasNext = true;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Utils.randomColor());
            eb.setTitle(commands.size() == 1 ? ("Help for, " + commands.get(0).getName() + ":") : "Commands:");
            for (int i = start; i < end; i++) {
                if( i < commands.size()) {
                    Command c = commands.get(i);
                    if (c != null) {
                        MessageEmbed.Field f = new MessageEmbed.Field(c.getName() + (!c.getFormat().isEmpty() ? (" " + c.getFormat() + ":") : ":"), c.getDescription() + (c.getFlags().isEmpty() ? "" : " >> ***Flags:*** " + c.getFlags()), false);
                        eb.addField(f);
                        try {
                            if (Utils.getEmbedLength(eb) >= MessageEmbed.EMBED_MAX_LENGTH_BOT - 100) {
                                eb.getFields().remove(f);
                                break;
                            }
                        } catch (Exception e) {
                            eb.getFields().remove(f);
                            break;
                        }
                    }
                }
                if(i == commands.size() - 1){
                    hasNext = false;
                    break;
                }
            }
            if (hasNext)
                eb.setFooter("To see more try `help " + (page + 1) + "`", null);
            sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
        }
    }
}
