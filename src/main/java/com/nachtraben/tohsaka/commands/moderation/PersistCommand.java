package com.nachtraben.tohsaka.commands.moderation;

import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.command.SubCommand;

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
                    BotConfig config = sendee.getDbot().getConfig();
                    if(!config.getDeveloperIDs().contains(sendee.getUserID()) && !config.getOwnerIDs().contains(sendee.getUserID())) {
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
