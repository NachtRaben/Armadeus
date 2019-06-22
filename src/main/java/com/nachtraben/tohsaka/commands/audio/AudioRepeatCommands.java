package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;

import java.util.Map;

public class AudioRepeatCommands {
    @Cmd(name = "repeattrack", format = "", description = "Repeats the currently playing track.", aliases = {"repeat"})
    public void repeatSingle(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            if(!manager.getScheduler().isPlaying()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Gotta play something to repeat it, add more tracks with `/play <search>`");
                return;
            }
            manager.getScheduler().setRepeatTrack(!manager.getScheduler().isRepeatTrack());
            sendee.sendMessage(ChannelTarget.MUSIC, "Repeating track: `" + manager.getScheduler().isRepeatTrack() + "`.");
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "repeatall", format = "", description = "Repeats the audio queue.", aliases = {"repeatqueue"})
    public void repeatTrack(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            if(!manager.getScheduler().isPlaying()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Gotta play something to repeat it, add more tracks with `/play <search>`");
                return;
            }
            manager.getScheduler().setRepeatQueue(!manager.getScheduler().isRepeatQueue());
            sendee.sendMessage(ChannelTarget.MUSIC, "Repeating all: `" + manager.getScheduler().isRepeatQueue() + "`.");
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
