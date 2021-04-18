package dev.armadeus.bot.commands.audio;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.TimeUtil;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Arrays;
import java.util.Map;

public class AudioSeekCommand extends Command {

    public AudioSeekCommand() {
        super("seek", "<time>", "Buffers/Scans to a certain point in the current track.");
        setAliases(Arrays.asList("scan", "buffer"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Armadeus.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();

            String time = args.get("time");

            AudioTrack currentTrack = manager.getPlayer().getPlayingTrack();
            if (currentTrack == null) {
                sender.sendMessage("You have to queue up music in order to seek in a song.");
                return;
            }

            if (!currentTrack.isSeekable()) {
                sender.sendMessage("I cannot seek in a stream.");
            }

            boolean seek = time.startsWith("+") || time.startsWith("-");
            long position = Math.abs(TimeUtil.parse(time.replaceAll("[+-]", "")));

            if (seek) {
                position = time.startsWith("+") ? manager.getPlayer().getTrackPosition() + position : manager.getPlayer().getTrackPosition() - position;
            }
            position = Math.max(0, Math.min(position, currentTrack.getDuration()));

            sender.sendMessage("Seeking to `" + TimeUtil.format(position) + "/" + TimeUtil.format(currentTrack.getDuration()) + "`");
            manager.getPlayer().seekTo(position);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
