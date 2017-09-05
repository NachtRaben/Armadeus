package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

public class CleanCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanCommands.class);

    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("MM/dd/yy-h:mma");
    private static final SimpleDateFormat DATE_TIME_ZONE = new SimpleDateFormat("MM/dd/yy-h:mma z");
    private static final SimpleDateFormat DATE = new SimpleDateFormat("MM/dd/yy");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("h:mma");
    private static Set<Long> purges = new HashSet<>();

    // TODO: Clean [amount] --date=date --time=time --silent // bot messages only
    // TODO: Clear/Purge [amount] --date=date --time=time --silent
    // TODO: prune {users} --date=date --time=time --silent

    @Cmd(name = "clean", format = "[amount]", description = "Cleans bot messages. Dates specified in EST.", flags = {"--date=", "--time=", "--silent", "-s"})
    public void clean(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
            boolean hasAmount = args.containsKey("amount");
            boolean hasDate = flags.containsKey("date");
            boolean hasTime = flags.containsKey("time");

            int amount = 100;
            Date date = null;

            // Can run
            if (!canRun(sendee)) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the manage_messages perm required to run that command.");
                return;
            }

            if (purges.contains(sendee.getGuild().getIdLong())) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                return;
            }

            // Parse amount
            if (hasAmount) {
                amount = getAmount(sendee, args.get("amount"), isSilent);
                if (amount <= 0)
                    return;
            }

            // Parse date
            if ((hasDate || hasTime) && hasAmount && !isSilent) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry time/date flags do not work if the amount is specified.");
                return;
            }

            if (!hasAmount && hasDate && hasTime) {
                date = getDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                if (date == null)
                    return;
            }

            purge(sendee, amount, date, Collections.singletonList(sendee.getJDA().getSelfUser().getIdLong()), isSilent);
        }
    }

    @Cmd(name = "purge", format = "[amount]", description = "Cleans all messages. Dates specified in EST.", flags = {"--date=", "--time=", "--silent", "-s"}, aliases = {"clear"})
    public void purge(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
            boolean hasAmount = args.containsKey("amount");
            boolean hasDate = flags.containsKey("date");
            boolean hasTime = flags.containsKey("time");

            int amount = 100;
            Date date = null;

            // Can run
            if (!canRun(sendee)) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the manage_messages perm required to run that command.");
                return;
            }

            if (purges.contains(sendee.getGuild().getIdLong())) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                return;
            }

            // Parse amount
            if (hasAmount) {
                amount = getAmount(sendee, args.get("amount"), isSilent);
                if (amount <= 0)
                    return;
            }

            // Parse date
            if ((hasDate || hasTime) && hasAmount && !isSilent) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry time/date flags do not work if the amount is specified.");
                return;
            }

            if (!hasAmount && hasDate && hasTime) {
                date = getDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                if (date == null)
                    return;
            }

            purge(sendee, amount, date, Collections.emptyList(), isSilent);
        }
    }

    @Cmd(name = "prune", format = "{users/messages}", description = "Cleans user specific messages", flags = {"--date=", "--time=", "--amount=", "--silent", "-s"})
    public void prune(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
            boolean hasAmount = flags.containsKey("amount");
            boolean hasDate = flags.containsKey("date");
            boolean hasTime = flags.containsKey("time");

            int amount = 100;
            Date date = null;

            // Can run
            if (!canRun(sendee)) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the manage_messages perm required to run that command.");
                return;
            }

            if (purges.contains(sendee.getGuild().getIdLong())) {
                if (!isSilent)
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                return;
            }

            // Parse amount
            if (hasAmount) {
                amount = getAmount(sendee, flags.get("amount"), isSilent);
                if (amount <= 0)
                    return;
            }

            // Parse date
            if ((hasDate || hasTime) && hasAmount && !isSilent) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry time/date flags do not work if the amount is specified.");
                return;
            }

            if (!hasAmount && hasDate && hasTime) {
                date = getDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                if (date == null)
                    return;
            }

            List<Long> ids = new ArrayList<>();
            for (User user : sendee.getMessage().getMentionedUsers())
                ids.add(user.getIdLong());

            for (String s : args.get("users/messages").split("\\s+")) {
                if (!s.startsWith("<") && !s.endsWith(">")) {
                    try {
                        ids.add(Long.parseLong(s));
                    } catch (NumberFormatException e) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but, " + s + " is not a valid ID i can work with.");
                        return;
                    }
                }
            }

            purge(sendee, amount, date, ids, isSilent);
        }
    }

    private Integer getAmount(GuildCommandSender sender, String amount, boolean silent) {
        try {
            Integer result = Integer.parseInt(amount);
            if (result <= 0)
                sender.sendMessage(ChannelTarget.GENERIC, "Sorry, but provided amounts must be `>0`.");
            return result;
        } catch (NumberFormatException e) {
            if (!silent)
                sender.sendMessage(ChannelTarget.GENERIC, "Sorry but `" + amount + "` isn't actually a number. -.-");
            return -1;
        }
    }

    private Date getDate(GuildCommandSender sender, String date, String time, boolean silent) {
        Date result = null;
        if (date != null && time != null) {
            try {
                result = DATE_TIME.parse(date + "-" + time);
            } catch (ParseException e) {
                if (!silent)
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you didn't specify a valid date/time combo, try something like `--date=07/21/17 --time=8:30am`.");
                return null;
            }
        } else if (date != null) {
            try {
                result = DATE.parse(date);
            } catch (ParseException e) {
                if (!silent)
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you didn't specify a valid date, try something like `--date=07/21/17`.");
                return null;
            }
        } else if (time != null) {
            try {
                result = new Date(TIME.parse(time).getTime() + OffsetDateTime.now().get(ChronoField.YEAR));
            } catch (ParseException e) {
                long millis = TimeUtil.stringToMillis(time);
                if (millis > 0) {
                    result = new Date(System.currentTimeMillis() - millis);
                } else if (silent) {
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you did not specify a valid time, try something like `--time=8:30am` or `--time=30m` for a time-frame.");
                    return null;
                }
            }
        }
        return result;
    }

    private boolean canRun(GuildCommandSender sender) {
        BotConfig config = sender.getDbot().getConfig();
        return sender.getMember().hasPermission(Permission.MESSAGE_MANAGE) || config.getDeveloperIDs().contains(sender.getUserID()) || config.getOwnerIDs().contains(sender.getUserID());
    }

    private void purge(GuildCommandSender sender, int amount, Date date, List<Long> ids, boolean silent) {
        // TODO: Limit how far back it will search for messages with amount
        // TODO: Limit how far date can go back
        purges.add(sender.getGuild().getIdLong());
        MessageHistory history = new MessageHistory(sender.getTextChannel());
        if (date != null) {
            // Deal with a specified date.
            OffsetDateTime time = OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            LOGGER.debug("Purging until " + time.toString());
            boolean finished = false;
            int repetitions = 0;
            while (!finished) {
                repetitions++;
                if (repetitions == 50) {
                    LOGGER.error("Scanning for too many messages!", new RuntimeException());
                    break;
                }
                LOGGER.debug("Purge cycles: " + repetitions);
                List<Message> massPurge = new ArrayList<>();
                List<Message> singlePurge = new ArrayList<>();
                List<Message> messages = history.retrievePast(100).complete();
                if (messages.isEmpty())
                    break;

                for (Message m : messages) {
                    if (m.getCreationTime().isAfter(time)) {
                        if (ids.contains(m.getAuthor().getIdLong()) || ids.isEmpty()) {
                            LOGGER.debug("Deleting message: " + m.getIdLong() + " from " + m.getCreationTime());
                            if (!m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                                massPurge.add(m);
                            else
                                singlePurge.add(m);
                        }
                    } else {
                        finished = true;
                        break;
                    }
                }
                if (massPurge.size() > 2) {
                    sender.getTextChannel().deleteMessages(massPurge).complete();
                } else {
                    singlePurge.addAll(massPurge);
                }
                if (!singlePurge.isEmpty()) {
                    singlePurge.forEach(message -> {
                        try {
                            message.delete().reason("Clear command.").complete();
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
            LOGGER.debug("Finished.");
            if (!silent)
                sender.sendMessage(ChannelTarget.GENERIC, "Cleared all messages after `" + DATE_TIME_ZONE.format(date) + "` in " + sender.getTextChannel().getAsMention());
        } else {
            // Deal with amount.
            int repetitions = 0;
            int count = amount;
            while (count > 0) {
                repetitions++;
                if (repetitions == 50) {
                    LOGGER.error("Scanning for too many messages!", new RuntimeException());
                    break;
                }
                LOGGER.debug("Purge cycles: " + repetitions);
                List<Message> massPurge = new ArrayList<>();
                List<Message> singlePurge = new ArrayList<>();
                boolean finished = true;
                for (Message m : history.retrievePast(Math.min(count, 100)).complete()) {
                    if (ids.contains(m.getAuthor().getIdLong()) || ids.isEmpty()) {
                        LOGGER.debug("Deleting message: " + m.getIdLong());
                        finished = false;
                        if (!m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
                            massPurge.add(m);
                        else
                            singlePurge.add(m);
                    }
                }
                if (massPurge.size() > 2) {
                    sender.getTextChannel().deleteMessages(massPurge).complete();
                    count -= massPurge.size();
                } else {
                    singlePurge.addAll(massPurge);
                }
                if (!singlePurge.isEmpty()) {
                    singlePurge.forEach(message -> {
                        try {
                            message.delete().reason("Clear command.").complete();
                        } catch (Exception ignored) {
                        }
                    });
                    count -= singlePurge.size();
                }
                if (finished)
                    break;
            }
            if (!silent)
                sender.sendMessage(ChannelTarget.GENERIC, "Cleared the last `" + (amount - count) + "` messages in " + sender.getTextChannel().getAsMention());
        }
        purges.remove(sender.getGuild().getIdLong());
    }
}
