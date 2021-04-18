package dev.armadeus.bot.commands.moderation;

import dev.armadeus.core.audio.TrackScheduler;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.CommandTree;
import dev.armadeus.command.command.SubCommand;

import java.util.Map;

public class PersistCommand extends CommandTree {
    public PersistCommand() {
        getChildren().add(new SubCommand("persist", "", "Gets the current persist state.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    TrackScheduler scheduler = sendee.getGuildConfig().getMusicManager().getScheduler();
                    sendee.sendMessage(ChannelTarget.MUSIC, "Persisting: `" + scheduler.isPersist() + "`.");
                }
            }
        });

        getChildren().add(new SubCommand("persist", "<boolean>", "Sets the persist state.") {
            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    BotConfig config = BotConfig.get();
                    if (!config.getDeveloperIds().contains(sendee.getUser().getIdLong()) && !config.getOwnerIds().contains(sendee.getUser().getIdLong())) {
                        sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but that feature isn't available to everyone.");
                        return;
                    }
                    TrackScheduler scheduler = sendee.getGuildConfig().getMusicManager().getScheduler();
                    if(!scheduler.isPlaying()) {
                        sendee.sendMessage(ChannelTarget.MUSIC, "Can't persist without any music to play.");
                        return;
                    }
                    boolean b = Boolean.parseBoolean(args.get("boolean"));
                    scheduler.setPersist(b);
                    sendee.sendMessage(ChannelTarget.MUSIC, "Persisting: `" + b + "`.");
                }
            }
        });
    }

}
