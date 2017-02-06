package com.nachtraben.commands;

import com.xilixir.fw.command.Command;
import com.xilixir.fw.command.sender.UserCommandSender;

import java.util.Map;
import java.util.Random;

import static com.xilixir.fw.utils.Utils.format;


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

    @Command(name = "8ball", format = "{rest}", description = "Queries the magical 8ball to answer your question.")
    public void eightball(UserCommandSender sender, Map<String, String> args) {
        if(args.get("rest") == null) return;
        if(!args.get("rest").endsWith("?")) {
            sender.getCommandMessage().getTextChannel().sendMessage("You have to actually ask me a question >~> try again.").queue();
        } else {
            sender.getCommandMessage().getTextChannel().sendMessage(format("%s, %s.", sender.getCommandMessage().getAuthor().getAsMention(), ballquotes[random.nextInt(ballquotes.length)])).queue();
        }
    }

}
