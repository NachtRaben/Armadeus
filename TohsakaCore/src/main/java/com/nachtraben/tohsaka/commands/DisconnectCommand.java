package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.GuildCommandSender;
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
            if(sendee.getGuild().getAudioManager().isConnected()) {
                sendee.getGuildConfig().getMusicManager().getScheduler().stop();
                sendee.getGuild().getAudioManager().closeAudioConnection();
                sendee.sendMessage(ChannelTarget.MUSIC, "Guess you don't want me listening anymore :c");
            } else {
                sendee.sendMessage(ChannelTarget.GENERIC, "I'm not connected, why did you feel the need to run that?");
            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
