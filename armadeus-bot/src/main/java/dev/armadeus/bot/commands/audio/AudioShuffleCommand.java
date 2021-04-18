package dev.armadeus.bot.commands.audio;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;

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
