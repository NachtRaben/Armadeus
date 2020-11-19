package com.nachtraben.armadeus.commands.moderation;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;

import java.util.Map;

public class DisconnectCommand extends Command {

    public DisconnectCommand() {
        super("disconnect", "", "Disconnects from the active channel.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = sendee.getGuildConfig().getMusicManager();
            if (manager.getLink().getChannel() != null) {
                manager.getScheduler().stop();
                manager.getLink().disconnect();
                sendee.sendMessage(ChannelTarget.MUSIC, "Guess you don't want me listening anymore :c");
            } else {
                sendee.sendMessage(ChannelTarget.GENERIC, "I'm not connected, why did you feel the need to run that?");
            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
