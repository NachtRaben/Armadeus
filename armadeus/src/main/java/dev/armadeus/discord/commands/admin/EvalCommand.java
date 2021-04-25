package dev.armadeus.discord.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.eval.Eval;
import groovy.lang.Tuple3;
import net.dv8tion.jda.api.MessageBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

public class EvalCommand extends DiscordCommand {

    @Conditions("developeronly")
    @CommandAlias("eval")
    public void eval(DiscordCommandIssuer user, @Default String output) {
        // This fuckery is because some newlines get consumed inside the code-block
        String raw = user.getMessage().getContentRaw();
        int startIndex = raw.indexOf("\n```\n");
        int endIndex = raw.lastIndexOf("\n```");
        if (startIndex == -1 || endIndex == -1) {
            user.sendMessage("Eval commands must be encased in a code block");
            return;
        }
        startIndex += 5;

        String script = raw.substring(startIndex, endIndex);

        Eval eval = new Eval(user, script);
        Tuple3<Object, String, Throwable> result = eval.run();
        MessageBuilder builder = new MessageBuilder();
        if (result.getV2() != null && !result.getV2().isBlank()) {
            builder.append("**Output:**");
            builder.appendCodeBlock(result.getV2(), "groovy");
        }
        if (result.getV1() != null) {
            builder.append("**Result:**");
            builder.appendCodeBlock(String.valueOf(result.getV1()), null);
        }
        if (result.getV3() != null) {
            builder.append("**Error:**");
            StringWriter writer = new StringWriter();
            result.getV3().printStackTrace(new PrintWriter(writer));
            builder.appendCodeBlock(String.valueOf(writer.toString()), "java");
        }
        user.sendMessage(builder.build());
    }
}
