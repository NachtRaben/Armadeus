package com.nachtraben.tohsaka.commands.dnd;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class d4 extends Command {

    public d4() {
        super("d4", "", "Rolls a D4 die");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Utils.randomColor());
            builder.setThumbnail("https://rolladie.net/images/dice/d4.png");
            int roll = ThreadLocalRandom.current().nextInt(1, 5);
            builder.setDescription("You rolled a " + roll);
            sendee.getMessageChannel().sendMessage(builder.build()).queue();
        }
    }
}
