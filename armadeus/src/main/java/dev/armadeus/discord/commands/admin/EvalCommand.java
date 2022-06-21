package dev.armadeus.discord.commands.admin;

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.eval.Eval;
import dev.armadeus.discord.util.eval.EvalResult;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class EvalCommand extends DiscordCommand {

    private static final Pattern CODEBLOCK = Pattern.compile("`{1,3}(.*?)`{1,3}", Pattern.DOTALL | Pattern.MULTILINE);

    @SneakyThrows
    @Private
    @Conditions("developeronly")
    @CommandPermission("dev.eval")
    @CommandAlias("eval")
    @Description("Developer command used to run realtime evaluations")
    @Default
    @CatchUnknown
    public void eval(DiscordCommandIssuer user, File scriptFile) {
        // This fuckery is because some newlines get consumed inside the code-block
        String script = String.join("\n", Files.readAllLines(scriptFile.toPath()));
        logger.warn("Executing Script:\n{}", script);
        Message m = user.getChannel().sendMessage("Processing... " + core.shardManager().getGuildById(317784247949590528L).getEmoteById(895763555893256242L).getAsMention()).complete();

        Eval eval = new Eval(user, script);
        EvalResult<Object, String, Throwable> result = eval.run();
        MessageBuilder builder = new MessageBuilder();
        logger.warn("Left: {}", result.getLeft());
        logger.warn("Middle: {}", result.getMiddle());
        logger.warn("Right: {}", result.getRight());
        if (result.getMiddle() != null && !result.getMiddle().isBlank()) {
            builder.append("**Output:**");
            builder.appendCodeBlock(result.getMiddle().substring(0, Math.min(1000, result.getMiddle().length())), "groovy");
        }
        if (result.getLeft() != null) {
            builder.append("**Result:**");
            builder.appendCodeBlock(String.valueOf(result.getLeft()), null);
        }
        if (result.getRight() != null) {
            builder.append("**Error:**\n");
            StringWriter writer = new StringWriter();
            result.getRight().printStackTrace(new PrintWriter(writer));
            builder.appendCodeBlock(String.valueOf(writer.toString()), "groovy");
        }
        if(builder.length() > Message.MAX_CONTENT_LENGTH) {
            user.getChannel().sendFile(builder.getStringBuilder().toString().getBytes(StandardCharsets.UTF_8), "eval.txt").queue();
        } else {
            if (builder.isEmpty())
                builder.append("Success");
            m.editMessage(builder.build()).queue();
        }
    }
}
