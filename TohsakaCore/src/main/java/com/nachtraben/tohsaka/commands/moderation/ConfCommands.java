package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfCommands.class);

    @Cmd(name = "conf", format = "set prefixes (prefixes)", description = "Sets the prefixes for the selected guild.")
    public void setPrefixes(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry " + sendee.getMember().getAsMention() + " but you just aren't good enough for that command.");
                return;
            }

            GuildConfig config = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong());
            if (args.containsKey("prefixes")) {
                String[] keys = args.get("prefixes").split("\\s+");
                Set<String> prefixes = new HashSet<>();
                prefixes.addAll(Arrays.asList(keys));
                config.setPrefixes(prefixes);
                config.save();
                sendee.sendMessage(ChannelTarget.GENERIC, "The prefixes are now: `" + config.getPrefixes() + "`.");
            } else {
                config.setPrefixes(Collections.emptySet());
                sendee.sendMessage(ChannelTarget.GENERIC, "The prefixes have been cleared.");
            }
        }
    }

    @Cmd(name = "conf", format = "delete messages <boolean>", description = "Sets whether or not to delete command messages.")
    public void deleteMessages(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            GuildConfig config = sendee.getDbot().getGuildManager().getConfigurationFor(sendee.getGuild());
            config.setDeleteCommands(Boolean.parseBoolean(args.get("boolean")));
            config.save();
            sendee.sendMessage(ChannelTarget.GENERIC, "Delete command messages: `" + config.shouldDeleteCommands() + "`.");

        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "conf", format = "logs set <type> [channel]", description = "Sets the logging channel for the target type.")
    public void logsSet(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            TextChannel channel = sendee.getTextChannel();
            if(!sendee.getMessage().getMentionedChannels().isEmpty())
                channel = sendee.getMessage().getMentionedChannels().get(0);

            ChannelTarget target = ChannelTarget.forName(args.get("type"));
            if(target == null) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but the valid target types are, `" + Arrays.toString(ChannelTarget.values()) + "`.");
                return;
            }

            GuildConfig config = sendee.getDbot().getGuildManager().getConfigurationFor(sendee.getGuild());
            switch (target) {
                case GENERIC:
                    config.setLogChannel(ChannelTarget.GENERIC, channel);
                    break;
                case MUSIC:
                    config.setLogChannel(ChannelTarget.MUSIC, channel);
                    break;
                case NSFW:
                    config.setLogChannel(ChannelTarget.NSFW, channel);
                    break;
                case BOT_ANNOUNCEMENT:
                    config.setLogChannel(ChannelTarget.BOT_ANNOUNCEMENT, channel);
                    break;
                case TWITCH_ANNOUNCEMENT:
                    config.setLogChannel(ChannelTarget.TWITCH_ANNOUNCEMENT, channel);
                    break;
            }
            config.save();
            sendee.sendMessage(ChannelTarget.GENERIC, String.format("`%s` logging channel is now set to %s.", target.toString(), channel.getAsMention()));
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "conf", format = "logs clear <type>", description = "Clears the logging channel for the target type.")
    public void logsClear(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            ChannelTarget target = ChannelTarget.forName(args.get("type"));
            if(target == null) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but the valid target types are, `" + Arrays.toString(ChannelTarget.values()) + "`.");
                return;
            }

            GuildConfig config = sendee.getDbot().getGuildManager().getConfigurationFor(sendee.getGuild());
            switch (target) {
                case GENERIC:
                    config.setLogChannel(ChannelTarget.GENERIC, null);
                    break;
                case MUSIC:
                    config.setLogChannel(ChannelTarget.MUSIC, null);
                    break;
                case NSFW:
                    config.setLogChannel(ChannelTarget.NSFW, null);
                    break;
                case BOT_ANNOUNCEMENT:
                    config.setLogChannel(ChannelTarget.BOT_ANNOUNCEMENT, null);
                    break;
                case TWITCH_ANNOUNCEMENT:
                    config.setLogChannel(ChannelTarget.TWITCH_ANNOUNCEMENT, null);
                    break;
            }
            config.save();
            sendee.sendMessage(ChannelTarget.GENERIC, String.format("`%s` logging channel is now unset.", target.toString()));
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "conf", format = "set cooldown <cooldown>", description = "Sets global cooldown for all commands performed per user.")
    public void setCooldown(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            if (!hasPerms(sendee)) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Sorry but you just aren't good enough for that command.");
                return;
            }

            GuildConfig config = sendee.getDbot().getGuildManager().getConfigurationFor(sendee.getGuild());
            long cooldown = TimeUtil.stringToMillis(args.get("cooldown"));
            if(cooldown != 0 && cooldown < 5000) {
                sendee.sendMessage("Sorry but the minimal cooldown is either 0 (to disable) or 5 seconds.");
                return;
            }
            config.setCooldown(cooldown);
            config.save();
            sendee.sendMessage(ChannelTarget.GENERIC, "Cooldown is now: `" + TimeUtil.fromLong(config.getCooldown(), TimeUtil.FormatType.STRING) + "`.");

        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private boolean hasPerms(GuildCommandSender sendee) {
        BotConfig botConfig = Tohsaka.getInstance().getConfig();
        if (sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || botConfig.getDeveloperIDs().contains(sendee.getUser().getIdLong()) || botConfig.getOwnerIDs().contains(sendee.getUser().getIdLong())) {
            return true;
        }
        return true;
    }

}
