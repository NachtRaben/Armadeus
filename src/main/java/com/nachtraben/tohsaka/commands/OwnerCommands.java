package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.ConsoleCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Cmd;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.utils.HasteBin;
import com.nachtraben.tohsaka.Tohsaka;
import com.nachtraben.tohsaka.eval.EvalEngine;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NachtRaben on 3/2/2017.
 */
public class OwnerCommands {

    public OwnerCommands() {
    }

    public boolean validateOwner(CommandSender sender) {
        if(sender instanceof GuildCommandSender && ((GuildCommandSender)sender).getUser().getId().equals("118255810613608451")) return true;
        if(sender instanceof ConsoleCommandSender) return true;
        sender.sendMessage("Really? >~> How about I don't let you do that. Bot creators only bud.");
        return false;
    }

    @Cmd(name = "owner", format = "set globallog", description = "Sets the global logging channel for the bot.")
    public void setLogChannel(CommandSender sender) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if (!validateOwner(sendee)) return;
            Tohsaka.getInstance().setGlobalLogChannel(sendee.getChannel());
            sendee.getChannel().sendMessage("Global logging channel has been updated!").queue();
        }

    }

    @Cmd(name = "eval", format = "{script}", description = "Evaluates the command as js.", flags = { "--js", "--groovy", "--sc"})
    public void eval(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(!validateOwner(sendee)) return;
            Map<String, Object> fields = new HashMap<>();

            fields.put("sender", sendee);
            // Guild Stuff
            fields.put("channel", sendee.getChannel());
            fields.put("guild", sendee.getGuild());
            fields.put("message", sendee.getMessage());
            // User Stuff
            fields.put("me", sendee.getUser());
            fields.put("bot", sendee.getJDA().getSelfUser());
            // Shard stuff
            fields.put("api", sendee.getJDA());

            EvalEngine engine = flags.containsKey("groovy") ? EvalEngine.GROOVY : flags.containsKey("js") ? EvalEngine.JAVASCRIPT : EvalEngine.GROOVY;
            String script = args.get("script").replace("`", "");
            System.out.println("script");
            Triple<Object, String, String> result = engine.eval(fields, Collections.emptyList(), EvalEngine.DEFAULT_IMPORTS, script);

            MessageBuilder builder = new MessageBuilder();

            if (result.getLeft() != null) {
                if(result.getLeft().toString().length() >= 1994) builder.append("Result too long, here's the paste instead: ").append(String.valueOf(new HasteBin(result.getLeft().toString()).getHaste()));
                else builder.appendCodeBlock(result.getLeft().toString(), "");
            }
            if (!result.getMiddle().isEmpty()) {
                System.out.println(result.getMiddle().length());
                if(result.getMiddle().length() >= 1994) builder.append("Output too long, here's the paste instead: ").append(String.valueOf(new HasteBin(result.getMiddle()).getHaste()));
                else builder.appendCodeBlock(result.getMiddle(), "");
            }
            if (!result.getRight().isEmpty()) {
                if(result.getRight().length() >= 1994) builder.append("Error too long, here's the paste instead: ").append(String.valueOf(new HasteBin(result.getRight()).getHaste()));
                else builder.appendCodeBlock(result.getRight(), "");
            }

            if (builder.isEmpty()) {
                if(sendee.getMessage() != null)
                    sendee.getMessage().addReaction(EmojiManager.getForAlias("heavy_check_mark").getUnicode()).queue();
            } else {
                for (final Message m : builder.buildAll(MessageBuilder.SplitPolicy.NEWLINE, MessageBuilder.SplitPolicy.SPACE, MessageBuilder.SplitPolicy.ANYWHERE)) {
                    sendee.getChannel().sendMessage(m).queue();
                }
            }
        }
    }
}
