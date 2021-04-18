package dev.armadeus.bot.commands.dnd;

import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.util.Utils;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RollCommand extends Command {

    public RollCommand() {
        super("roll", "<die>", "Rolls a dice");
        super.setAliases(Collections.singletonList("dice"));
        super.setFlags(Arrays.asList("-p", "--percent"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            String[] tokens = args.get("die").split("[dD]");
            int attempts = tokens.length == 2 ? Integer.parseInt(tokens[0]) : 1;
            int die = tokens.length == 2 ? Integer.parseInt(tokens[1]) : Integer.parseInt(tokens[0]);
            boolean percentage = flags.containsKey("p") || flags.containsKey("percentage");
            boolean special = die == 4 || die == 8 || die == 10 || die == 12 || die == 20 || die == 100;
            boolean regular = die < 7 && !special;
            String image = null;
            if (special) {
                image = "https://rolladie.net/images/dice/d" + die + ".png";
            } else if (regular) {
                image = "https://rolladie.net/images/dice/dice" + die + ".jpg";
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Utils.randomColor());
            builder.setThumbnail(image);
            StringBuilder sb = new StringBuilder();
            sb.append("Rolls:\n");
            int total = 0;
            for (int i = 0; i < attempts; i++) {
                int roll = ThreadLocalRandom.current().nextInt(1, die + 1);
                total += roll;
                sb.append("`").append(percentage ? roll * 10 + "%" : roll).append("` ");
            }
            if (attempts > 1) {
                sb.append("\nTotal: \n`").append(percentage ? total * 10 + "%" : total).append("`");
            }
            builder.setDescription(sb.toString());
            builder.setFooter("Rolled by " + sendee.getName(), sendee.getUser().getAvatarUrl());
            sendee.getMessageChannel().sendMessage(builder.build()).queue();
        }
    }
}
