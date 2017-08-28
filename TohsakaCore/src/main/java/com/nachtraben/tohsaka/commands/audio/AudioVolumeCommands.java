package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;

import java.util.Map;

public class AudioVolumeCommands {

    @Cmd(name = "volume", format = "", description = "Get the current volume level.", aliases = {"vol"})
    public void volume(CommandSender sender) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            sender.sendMessage("The volume is currently `" + manager.getPlayer().getVolume() + "/150`.");
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "volume", format = "<vol>", description = "Sets the volume.", aliases = {"vol"})
    public void volumeSet(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            int vol = -1;
            try {
                vol = Integer.parseInt(args.get("vol"));
            } catch (NumberFormatException e) {
                sendee.sendMessage(ChannelTarget.GENERIC, "`" + args.get("vol") + "` is not a proper number -.-");
                return;
            }
            if (vol != -1) {
                manager.getPlayer().setVolume(vol);
                sendee.sendMessage(ChannelTarget.GENERIC, "Volume set to `" + manager.getPlayer().getVolume() + "/150`.");
            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
