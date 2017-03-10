package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Cmd;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.utils.LogManager;

import java.util.Map;
import java.util.Random;

import static com.nachtraben.core.utils.StringUtils.format;

/**
 * Created by NachtRaben on 2/3/2017.
 */
public class MiscCommands {

    private static String[] ballquotes;
    private Random random;

    public MiscCommands() {
        ballquotes = new String[]{
                "It is certain",
                "It is decidedly so",
                "Without a doubt",
                "Yes, definitely",
                "You may rely on it",
                "As I see it, yes",
                "Most likely",
                "Outlook good",
                "Yes",
                "Signs point to yes",
                "Reply hazy try again",
                "Ask again later",
                "Better not tell you now",
                "Cannot predict now",
                "Concentrate and ask again",
                "Don't count on it",
                "My reply is no",
                "My sources say no",
                "Outlook not so good",
                "Very doubtful",
        };
        random = new Random();
    }

    @Cmd(name = "8ball", format = "{rest}", description = "Queries the magical 8ball for you.")
    public void eightball(CommandSender sender, Map<String, String> args) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            LogManager.TOHSAKA.info(args.get("rest"));
            if (args.get("rest") == null) return;
            System.out.println(args.get("rest"));
            if (!args.get("rest").endsWith("?")) {
                sendee.getMessage().getTextChannel().sendMessage("You have to actually ask me a question >~> try again.").queue();
            } else {
                sendee.getMessage().getTextChannel().sendMessage(format("%s, %s.", sendee.getMessage().getAuthor().getAsMention(), ballquotes[random.nextInt(ballquotes.length)])).queue();
            }
        }
    }
}
