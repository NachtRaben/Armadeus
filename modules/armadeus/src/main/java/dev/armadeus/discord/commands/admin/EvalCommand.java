package dev.armadeus.discord.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.DiscordPermission;
import co.aikar.commands.annotation.Private;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.eval.Eval;
import dev.armadeus.discord.util.eval.EvalResult;
import groovy.lang.Tuple3;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class EvalCommand extends DiscordCommand {

    @Private
    @Conditions("developeronly")
    @CommandPermission("dev.eval")
    @CommandAlias("eval")
    @Description("Developer command used to run realtime evaluations")
    @Default
    public void eval(DiscordCommandIssuer user) {
        // This fuckery is because some newlines get consumed inside the code-block
        String raw = user.getMessage().getContentRaw();
        int startIndex = raw.indexOf("```");
        int endIndex = raw.lastIndexOf("```");
        if (startIndex == -1 || endIndex == -1 || startIndex == endIndex) {
            user.sendMessage("Eval commands must be encased in a code block");
            return;
        }
        startIndex += 5;

        String script = raw.substring(startIndex, endIndex);

        Message m = user.getChannel().sendMessage("Processing... " + core.shardManager().getGuildById(317784247949590528L).getEmoteById(895763555893256242L).getAsMention()).complete();

        Eval eval = new Eval(user, script);
        EvalResult<Object, String, Throwable> result = eval.run();
        MessageBuilder builder = new MessageBuilder();
        if (result.getMiddle() != null && !result.getMiddle().isBlank()) {
            if(result.getMiddle().length() < 1000) {
                builder.append("**Output:**");
                builder.appendCodeBlock(result.getMiddle(), "groovy");
            }
        }
        if (result.getLeft() != null) {
            builder.append("**Result:**");
            builder.appendCodeBlock(String.valueOf(result.getLeft()), null);
        }
        if (result.getRight() != null) {
            builder.append("**Error:**");
            StringWriter writer = new StringWriter();
            result.getRight().printStackTrace(new PrintWriter(writer));
            builder.appendCodeBlock(String.valueOf(writer.toString()), "java");
        }
        if(builder.length() > Message.MAX_CONTENT_LENGTH) {
            user.getChannel().sendFile(builder.getStringBuilder().toString().getBytes(StandardCharsets.UTF_8), "eval.txt").queue();
        } else {
            m.editMessage(builder.build()).queue();
        }
    }
}
