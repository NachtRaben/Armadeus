package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Map;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "", "Shows the bots ping.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            long duration = sendee.getUser().getJDA().getRestPing().complete();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Ping: ");
            eb.setColor(Utils.randomColor());
            eb.addField("API " + EmojiManager.getForAlias("ping_pong").getUnicode(), duration + " ms.", true);
            eb.addField("WebSocket " + EmojiManager.getForAlias("musical_note").getUnicode(), sendee.getUser().getJDA().getGatewayPing() + " ms.", true);
            sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
        }
    }

}
