package dev.armadeus.bot.commands.audio;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;

import java.util.Map;

public class AudioRepeatCommands {
    @Cmd(name = "repeattrack", format = "", description = "Repeats the currently playing track.", aliases = {"repeat"})
    public void repeatSingle(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Armadeus.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
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
            GuildMusicManager manager = Armadeus.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
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
