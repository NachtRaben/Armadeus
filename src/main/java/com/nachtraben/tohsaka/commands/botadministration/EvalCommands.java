package com.nachtraben.tohsaka.commands.botadministration;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.eval.Eval;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;

import java.util.HashMap;
import java.util.Map;

public class EvalCommands {

    @Cmd(name = "eval", format = "{script}", description = "Runs an evaluation.")
    public void eval(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            BotConfig botConfig = sendee.getDbot().getConfig();
            if (!botConfig.getOwnerIDs().contains(sendee.getUser().getIdLong())) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but you're not allowed to use this.");
                return;
            }

//for(g in sender.getJDA().getGuilds() { List<Role> roles = g.getMember(sender.getUser());  g.getController().removeRolesFromMember(g.getMember(sender.getUser()), roles)}
            Map<String, Object> vars = new HashMap<>();
            if(sendee instanceof GuildCommandSender) {
                GuildCommandSender gs = (GuildCommandSender) sendee;
//                gs.getGuild().getController().removeRolesFromMember()
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
            if(response != null && !response.isEmpty()) {
                sendee.sendMessage(ChannelTarget.GENERIC, response);
            }
        }
    }

}
