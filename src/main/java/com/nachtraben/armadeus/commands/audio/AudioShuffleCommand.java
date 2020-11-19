package com.nachtraben.armadeus.commands.audio;

import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;

import java.util.Map;

public class AudioShuffleCommand extends Command {

    public AudioShuffleCommand() {
        super("shuffle", "", "Shuffles the audio queue.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Armadeus.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            if(manager.getScheduler().shuffle()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "The queue has been shuffled!");
            } else {
                sendee.sendMessage(ChannelTarget.MUSIC, "Can't shuffle an empty queue, add more tracks with `/play <search>`");
            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
