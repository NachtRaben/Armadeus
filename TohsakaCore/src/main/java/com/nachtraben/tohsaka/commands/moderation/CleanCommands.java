package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CleanCommands {

    @Cmd(name = "clean", format = "[amount]", description = "Cleans the chat of bot messages", aliases = {"clear"})
    public void clean(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            long id = sendee.getUser().getIdLong();
            BotConfig config = Tohsaka.getInstance().getConfig();
            Member bot = sendee.getGuild().getMember(sendee.getGuild().getJDA().getSelfUser());
            if(!bot.hasPermission(Permission.MESSAGE_MANAGE)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but the bot is missing the `" + Permission.MESSAGE_MANAGE + "` perm required for this command.");
                return;
            }

            if (!sendee.getMember().hasPermission(Permission.MESSAGE_MANAGE) || !config.getOwnerIDs().contains(id) || !config.getDeveloperIDs().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you aren't privvy enough for that command.");
                return;
            }

            int amount = 100;
            if (args.containsKey("amount")) {
                try {
                    amount = Integer.parseInt(args.get("amount"));
                } catch (NumberFormatException e) {
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but `" + args.get("amount") + "` isn't a number bud, go back to school.");
                    return;
                }
            }
            clean(sendee, amount, Collections.emptyList());
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "clean", format = "<amount> {users}", description = "Cleans the chat of user messages", aliases = {"clear"})
    public void cleanUsers(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            long id = sendee.getUser().getIdLong();
            BotConfig config = Tohsaka.getInstance().getConfig();

            if (!sendee.getMember().hasPermission(Permission.MESSAGE_MANAGE) || !config.getOwnerIDs().contains(id) || !config.getDeveloperIDs().contains(id)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you aren't privvy enough for that command.");
                return;
            }

            int amount = 100;
            try {
                amount = Integer.parseInt(args.get("amount"));
            } catch (NumberFormatException e) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but `" + args.get("amount") + "` isn't a number bud, go back to school.");
                return;
            }

            List<Long> ids = new ArrayList<>();
            for (User u : sendee.getMessage().getMentionedUsers())
                ids.add(u.getIdLong());

            for (String s : args.get("users").split("\\s+")) {
                if (s.startsWith("<") && s.endsWith(">"))
                    continue;
                try {
                    ids.add(Long.parseLong(s));
                } catch (NumberFormatException e) {
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but `" + args.get("amount") + "` isn't a number bud, go back to school.");
                    return;
                }
            }

            clean(sendee, amount, ids);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private void clean(GuildCommandSender sender, int amount, List<Long> ids) {
        while (amount > 100) {

            List<Message> toDelete = new ArrayList<>();
            for (Message m : sender.getTextChannel().getHistory().retrievePast(100).complete()) {
                if (ids.isEmpty() && m.getAuthor().equals(sender.getGuild().getJDA().getSelfUser()) && !m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                    toDelete.add(m);
                else if (ids.contains(m.getAuthor().getIdLong()) && !m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                    toDelete.add(m);
            }
            if (toDelete.size() > 2) {
                sender.getTextChannel().deleteMessages(toDelete).complete();
            } else {
                toDelete.forEach(m2 -> m2.delete().reason("Clear command").complete());
            }
            amount -= 100;
        }

        List<Message> toDelete = new ArrayList<>();
        for (Message m : sender.getTextChannel().getHistory().retrievePast(amount).complete()) {
            if (ids.isEmpty() && m.getAuthor().equals(sender.getGuild().getJDA().getSelfUser()) && !m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                toDelete.add(m);
            else if (ids.contains(m.getAuthor().getIdLong()) && !m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                toDelete.add(m);
        }
        if (toDelete.size() > 2) {
            sender.getTextChannel().deleteMessages(toDelete).complete();
        } else {

            toDelete.forEach(m2 -> m2.delete().reason("Clear command").complete());
        }
        sender.sendMessage(ChannelTarget.GENERIC, "Cleared the chat of " + sender.getTextChannel().getAsMention() + ".");
    }
}
