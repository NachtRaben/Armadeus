package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.Map;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "", "Shows the bots ping.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            Long start = System.currentTimeMillis();
            try {
                sendee.getMessageChannel().sendTyping().complete(false);
                long duration = System.currentTimeMillis() - start;
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Ping: ");
                eb.setColor(Utils.randomColor());
                eb.addField("API " + EmojiManager.getForAlias("ping_pong").getUnicode(), duration + " ms.", true);
                eb.addField("WebSocket " + EmojiManager.getForAlias("musical_note").getUnicode(), sendee.getUser().getJDA().getPing() + " ms.", true);
                sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
            } catch (RateLimitedException e) {
                sendee.sendMessage("Sorry but I can't ping right now.");
            }
        }
    }

}
