package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.DateTimeUtil;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.command.SubCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CleanCommands extends CommandTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanCommands.class);
    private static Set<Long> purges = new HashSet<>();

    // TODO: Clean [amount] --date=date --time=time --silent --message=
    // TODO: Clear/Purge [amount] --date=date --time=time --silent --message=
    // TODO: prune {users} --date=date --time=time --silent --message=

    public CleanCommands() {
        super.getChildren().add(new SubCommand("clean", "[amount]", "Cleans bot messages. Dates specified in EST.") {
            @Override
            public void init() {
                super.setFlags(Arrays.asList("--date=", "--time=", "--message=", "--silent", "--preserve", "-sp"));
            }

            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if (sender instanceof GuildCommandSender) {
                    LOGGER.debug("CLEAN COMMAND!!");
                    GuildCommandSender sendee = (GuildCommandSender) sender;

                    boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
                    boolean hasAmount = args.containsKey("amount");
                    boolean hasDate = flags.containsKey("date");
                    boolean hasTime = flags.containsKey("time");
                    boolean hasMessage = flags.containsKey("message");

                    int amount = 100;
                    long messageId = -1;
                    LocalDateTime date = null;

                    // Can run
                    if (!canRun(sendee)) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the " + Permission.MESSAGE_MANAGE.getName().toLowerCase() + " perm required to run that command.");
                        return;
                    }

                    if (purges.contains(sendee.getGuild().getIdLong())) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                        return;
                    }

                    // Parse amount
                    if (hasAmount) {
                        LOGGER.debug("amount");
                        amount = parseAmount(sendee, args.get("amount"), isSilent);
                        if (amount <= 0)
                            return;
                    }
                    // Parse message id
                    else if (hasMessage) {
                        LOGGER.debug("message");
                        try {
                            messageId = Long.parseLong(flags.get("message"));
                        } catch (NumberFormatException e) {
                            if (!isSilent)
                                sendee.sendMessage("You didn't provide a valid channel ID. The ID of your message was `" + sendee.getMessage().getIdLong() + "`.");
                            return;
                        }
                        Message m = sendee.getTextChannel().getMessageById(messageId).complete();
                        if (m == null) {
                            if (!isSilent)
                                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but the message you asked for doesn't exist.");
                            return;
                        }
                    }
                    // Parse date/time
                    else if (hasDate || hasTime) {
                        LOGGER.debug("date/time");
                        date = parseDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                        if (date == null)
                            return;
                    }

                    purge(sendee, amount, date, messageId, Collections.singletonList(sendee.getJDA().getSelfUser().getIdLong()), Collections.emptyList(), isSilent, flags.containsKey("preserve") || flags.containsKey("p"));
                }
            }
        });
        super.getChildren().add(new SubCommand("purge", "[amount]", "Cleans all messages. Dates specified in EST.") {
            @Override
            public void init() {
                super.setFlags(Arrays.asList("--date=", "--time=", "--message=", "--silent", "--preserve", "-sp"));
            }

            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if (sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;

                    boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
                    boolean hasAmount = args.containsKey("amount");
                    boolean hasDate = flags.containsKey("date");
                    boolean hasTime = flags.containsKey("time");
                    boolean hasMessage = flags.containsKey("message");

                    int amount = 100;
                    long messageId = -1;
                    LocalDateTime date = null;

                    if (!sendee.getGuild().getMember(sendee.getJDA().getSelfUser()).hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I don't have permission to delete messages here.");
                        return;
                    }

                    // Can run
                    if (!canRun(sendee)) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the " + Permission.MESSAGE_MANAGE.getName().toLowerCase() + " perm required to run that command.");
                        return;
                    }

                    if (purges.contains(sendee.getGuild().getIdLong())) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                        return;
                    }

                    // Parse amount
                    if (hasAmount) {
                        LOGGER.debug("amount");
                        amount = parseAmount(sendee, args.get("amount"), isSilent);
                        if (amount <= 0)
                            return;
                    }
                    // Parse message id
                    else if (hasMessage) {
                        LOGGER.debug("message");
                        try {
                            messageId = Long.parseLong(flags.get("message"));
                        } catch (NumberFormatException e) {
                            if (!isSilent)
                                sendee.sendMessage("You didn't provide a valid channel ID. The ID of your message was `" + sendee.getMessage().getIdLong() + "`.");
                            return;
                        }
                        Message m = sendee.getTextChannel().getMessageById(messageId).complete();
                        if (m == null) {
                            if (!isSilent)
                                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but the message you asked for doesn't exist.");
                            return;
                        }
                    }
                    // Parse date/time
                    else if (hasDate || hasTime) {
                        LOGGER.debug("date/time");
                        date = parseDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                        if (date == null)
                            return;
                    }

                    purge(sendee, amount, date,  messageId, Collections.emptyList(), Collections.emptyList(), isSilent, flags.containsKey("preserve") || flags.containsKey("p"));
                }
            }
        });
        super.getChildren().add(new SubCommand("prune", "{users/content}", "Cleans messages based on users/content. Dates specified in EST.") {
            @Override
            public void init() {
                super.setFlags(Arrays.asList("--date=", "--time=", "--message=", "--amount=", "--silent",  "--preserve", "-sp"));
            }

            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if (sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;

                    boolean isSilent = flags.containsKey("silent") || flags.containsKey("s");
                    boolean hasAmount = flags.containsKey("amount");
                    boolean hasDate = flags.containsKey("date");
                    boolean hasTime = flags.containsKey("time");
                    boolean hasMessage = flags.containsKey("message");

                    int amount = 100;
                    long messageId = -1;
                    LocalDateTime date = null;

                    if (!sendee.getGuild().getMember(sendee.getJDA().getSelfUser()).hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I don't have permission to delete messages here.");
                        return;
                    }

                    // Can run
                    if (!canRun(sendee)) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "You are missing the " + Permission.MESSAGE_MANAGE.getName().toLowerCase() + " perm required to run that command.");
                        return;
                    }

                    if (purges.contains(sendee.getGuild().getIdLong())) {
                        if (!isSilent)
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but there is already an active purge for this guild.");
                        return;
                    }

                    // Parse amount
                    if (hasAmount) {
                        LOGGER.debug("amount");
                        amount = parseAmount(sendee, flags.get("amount"), isSilent);
                        if (amount <= 0)
                            return;
                    }
                    // Parse message id
                    else if (hasMessage) {
                        LOGGER.debug("message");
                        try {
                            messageId = Long.parseLong(flags.get("message"));
                        } catch (NumberFormatException e) {
                            if (!isSilent)
                                sendee.sendMessage("You didn't provide a valid channel ID. The ID of your message was `" + sendee.getMessage().getIdLong() + "`.");
                            return;
                        }
                        Message m = sendee.getTextChannel().getMessageById(messageId).complete();
                        if (m == null) {
                            if (!isSilent)
                                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but the message you asked for doesn't exist.");
                            return;
                        }
                    }
                    // Parse date/time
                    else if (hasDate || hasTime) {
                        LOGGER.debug("date/time");
                        date = parseDate(sendee, flags.get("date"), flags.get("time"), isSilent);
                        if (date == null)
                            return;
                    }

                    List<Long> ids = new ArrayList<>();
                    List<String> filters = new ArrayList<>();
                    for (User user : sendee.getMessage().getMentionedUsers())
                        ids.add(user.getIdLong());

                    for (String s : args.get("users/content").split("\\s+")) {
                        if (!s.startsWith("<") && !s.endsWith(">")) {
                            try {
                                ids.add(Long.parseLong(s));
                            } catch (NumberFormatException e) {
                                filters.add(s);
                            }
                        }
                    }

                    purge(sendee, amount, date, messageId, ids, filters, isSilent, flags.containsKey("preserve") || flags.containsKey("p"));
                }
            }
        });
    }

    private Integer parseAmount(GuildCommandSender sender, String amount, boolean silent) {
        try {
            Integer result = Integer.parseInt(amount);
            if (result <= 0)
                sender.sendMessage(ChannelTarget.GENERIC, "Sorry, but provided amounts must be `>0`.");
            return result;
        } catch (NumberFormatException e) {
            if (!silent) {
                if(amount.startsWith("<") && amount.endsWith(">"))
                    sender.sendMessage(ChannelTarget.GENERIC, "Seems like you tried to specify a user, you want to use the prune command.");
                else
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but `" + amount + "` isn't actually a number. -.-");
            }
            return -1;
        }
    }

    private LocalDateTime parseDate(GuildCommandSender sender, String date, String time, boolean silent) {
        LOGGER.debug("Date: " + date + "\tTime: " + time);
        LocalDateTime result = null;
        if (date != null && time != null) {
            LOGGER.debug("DATE-TIME");
            result = DateTimeUtil.parseDateTime(date, time);
            if (result == null)
                if (!silent)
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you didn't specify a valid date/time combo, try something like `--date=07/21/17 --time=8:30am`.");
        } else if (date != null) {
            LOGGER.debug("DATE");
            LocalDate parsedDate = DateTimeUtil.parseDate(date);
            if (parsedDate == null) {
                if (!silent)
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you didn't specify a valid date, try something like `--date=07/21/17`.");
            } else {
                result = LocalDateTime.of(parsedDate, LocalTime.of(0, 0));
            }
        } else if (time != null) {
            LOGGER.debug("TIME");
            LocalTime parsedTime = DateTimeUtil.parseTime(time);
            if (parsedTime != null) {
                result = LocalDateTime.of(LocalDate.now(), parsedTime);
            } else {
                long millis = TimeUtil.stringToMillis(time);
                if (millis > 0)
                    result = LocalDateTime.of(LocalDate.now(), LocalTime.now().minusSeconds(TimeUnit.MILLISECONDS.toSeconds(millis)));
                else if (!silent)
                    sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you did not specify a valid time, try something like `--time=8:30am` or `--time=30m` for a time-frame.");
            }
        }
        LOGGER.debug(String.valueOf(result));
        if (result != null && result.isBefore(LocalDateTime.now().minusWeeks(4))) {
            if (!silent)
                sender.sendMessage(ChannelTarget.GENERIC, "Sorry but you can't perform a date purge on anything older than a month.");
            result = null;
        }
        return result;
    }

    private boolean canRun(GuildCommandSender sender) {
        BotConfig config = sender.getDbot().getConfig();
        return sender.getMember().hasPermission(Permission.MESSAGE_MANAGE) || config.getDeveloperIDs().contains(sender.getUserID()) || config.getOwnerIDs().contains(sender.getUserID());
    }

    private void purge(GuildCommandSender sender, int amount, LocalDateTime date, long messageID, List<Long> ids, List<String> filters, boolean silent, boolean preserve) {
        // TODO: Limit how far back it will search for messages with amount
        // TODO: Limit how far date can go back
        purges.add(sender.getGuild().getIdLong());
        boolean ignore = sender.getGuildConfig().shouldDeleteCommands();

        OffsetDateTime time = date != null ? date.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;

        int deletions = 0;
        int cycles = 5;
        boolean finished = false;
        List<Message> bulkDelete = new ArrayList<>();
        List<Message> singleDelete = new ArrayList<>();
        MessageHistory history = new MessageHistory(sender.getTextChannel());
        if (date != null) {
            // TODO: Deal with date
            LOGGER.debug(time.toString());
            while (!finished) {
                if (cycles <= 0) {
                    if (!silent)
                        sender.sendMessage(ChannelTarget.GENERIC, "Sorry, but you have hit the purge limit with `" + deletions + "` deletions.");
                    LOGGER.error("Scanning for too many messages!", new RuntimeException());
                    break;
                }
                LOGGER.debug("Cycles remaining: " + cycles);
                List<Message> messages = history.retrievePast(100).complete();
                if (messages.isEmpty())
                    break;

                for (Message m : messages) {
                    if (m.getCreationTime().isAfter(time)) {
                        if (ignore && m.equals(sender.getMessage()))
                            continue;

                        if (shouldDelete(m, ids, filters, preserve)) {
                            LOGGER.debug("Deleting message: " + m.getIdLong() + " from " + m.getCreationTime().toString());
                            processMessage(m, bulkDelete, singleDelete);
                        }
                    } else {
                        LOGGER.debug("Finished, found message before Date.");
                        finished = true;
                        break;
                    }
                }
                cycles--;
                deletions += bulkDelete.size();
                deletions += singleDelete.size();
                delete(sender, bulkDelete, singleDelete);
            }
        } else if (messageID > 0) {
            LOGGER.debug("Deleting until I found message: " + messageID);
            while (!finished) {
                if (cycles <= 0) {
                    if (!silent)
                        sender.sendMessage(ChannelTarget.GENERIC, "Sorry, but you have hit the purge limit with `" + deletions + "` deletions.");
                    LOGGER.error("Scanning for too many messages!", new RuntimeException());
                    break;
                }
                LOGGER.debug("Cycles remaining: " + cycles);
                List<Message> messages = history.retrievePast(100).complete();
                if (messages.isEmpty())
                    break;

                for (Message m : messages) {
                    if (m.getIdLong() != messageID) {
                        if (ignore && m.equals(sender.getMessage()))
                            continue;

                        if (shouldDelete(m, ids, filters, preserve)) {
                            LOGGER.debug("Deleting message: " + m.getIdLong());
                            processMessage(m, bulkDelete, singleDelete);
                        }
                    } else {
                        LOGGER.debug("Finished, found message with ID.");
                        finished = true;
                        break;
                    }
                }
                cycles--;
                deletions += bulkDelete.size();
                deletions += singleDelete.size();
                delete(sender, bulkDelete, singleDelete);
            }
        } else if (amount > 0) {
            // Deal with amount.
            while (amount > 0) {
                if (cycles <= 0) {
                    if (!silent)
                        sender.sendMessage(ChannelTarget.GENERIC, "Sorry, but you have hit the purge limit with `" + deletions + "` deletions.");
                    LOGGER.error("Scanning for too many messages!", new RuntimeException());
                    break;
                }
                LOGGER.debug("Cycles remaining: " + cycles);
                List<Message> messages = history.retrievePast(Math.min(100, amount)).complete();
                if (messages.isEmpty())
                    break;

                for (Message m : messages) {
                    if (ignore && m.equals(sender.getMessage()))
                        continue;

                    if (shouldDelete(m, ids, filters, preserve)) {
                        LOGGER.debug("Deleting message: " + m.getIdLong());
                        processMessage(m, bulkDelete, singleDelete);
                    }
                }
                LOGGER.debug("Amount: " + amount);
                cycles--;
                deletions += bulkDelete.size();
                deletions += singleDelete.size();
                amount -= Math.min(100, amount);
                delete(sender, bulkDelete, singleDelete);
            }
            LOGGER.debug("Finished, processed Amount.");
        }
        if(!silent) {
            sender.sendMessage(ChannelTarget.GENERIC, "Purged `" + deletions + "` messages in " + sender.getTextChannel().getAsMention() + (date != null ? (" since `" + date.toString() + "`.") : ""));
        }
        purges.remove(sender.getGuild().getIdLong());
    }

    private void processMessage(Message m, List<Message> bulkDelete, List<Message> singleDelete) {
        if (!m.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2)))
            bulkDelete.add(m);
        else
            singleDelete.add(m);
    }

    private boolean shouldDelete(Message m, List<Long> ids, List<String> filters, boolean preserve) {
        // Image preservation
        if(preserve) {
            for (Message.Attachment att : m.getAttachments())
                if (att.isImage())
                    return false;

            for (MessageEmbed e : m.getEmbeds())
                if (e.getImage() != null)
                    return false;
        }

        // Author matching
        if (ids.isEmpty() || ids.contains(m.getAuthor().getIdLong()) && filters.isEmpty())
            return true;

        // Filter matching
        else if (!filters.isEmpty())
            for (String filter : filters)
                if (m.getContentRaw().contains(filter))
                    return true;

        return false;
    }

    private void delete(GuildCommandSender sender, List<Message> bulkDelete, List<Message> singleDelete) {
        if (bulkDelete.size() > 2) {
            try {
                sender.getTextChannel().deleteMessages(bulkDelete).complete();
            } catch (Exception ignored) {
                LOGGER.debug("Failed to bulk delete messages", ignored);
            }
        } else {
            singleDelete.addAll(bulkDelete);
        }
        singleDelete.forEach(message -> {
            try {
                message.delete().reason("Clear command.").complete();
            } catch (Exception ignored) {
                LOGGER.debug("Failed to delete message: " + message.getContentRaw(), ignored);
            }
        });
        bulkDelete.clear();
        singleDelete.clear();
    }
}
