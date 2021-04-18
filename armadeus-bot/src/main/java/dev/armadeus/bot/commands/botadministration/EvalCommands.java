package dev.armadeus.bot.commands.botadministration;

import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.eval.Eval;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;

import java.util.HashMap;
import java.util.Map;

public class EvalCommands {

    @Cmd(name = "eval", format = "{script}", description = "Runs an evaluation.")
    public void eval(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            BotConfig botConfig = BotConfig.get();
            if (!botConfig.getOwnerIds().contains(sendee.getUser().getIdLong())) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but you're not allowed to use this.");
                return;
            }

            Map<String, Object> vars = new HashMap<>();
            if (sendee instanceof GuildCommandSender) {
                GuildCommandSender gs = (GuildCommandSender) sendee;
                vars.put("sender", gs);
                vars.put("guild", gs.getGuild());
                vars.put("channel", gs.getTextChannel());
            } else {
                vars.put("sender", sendee);
            }
            vars.put("message", sendee.getMessage());
            vars.put("me", sendee.getUser());
            vars.put("jda", sendee.getUser().getJDA());
            vars.put("bot", sendee.getUser().getJDA().getSelfUser());

            Eval eval = new Eval(args.get("script"), vars);

            String response = eval.run();
            if (response != null && !response.isEmpty()) {
                sendee.sendMessage(ChannelTarget.GENERIC, response);
            }
        }
    }

}
