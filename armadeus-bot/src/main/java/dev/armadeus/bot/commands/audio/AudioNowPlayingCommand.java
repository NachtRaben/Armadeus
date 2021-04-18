package dev.armadeus.bot.commands.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
import dev.armadeus.core.audio.TrackScheduler;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.TimeUtil;
import dev.armadeus.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Arrays;
import java.util.Map;

public class AudioNowPlayingCommand extends Command {
    public AudioNowPlayingCommand() {
        super("np", "", "Shows the currently playing track and player info.");
        setFlags(Arrays.asList("-e", "--extended"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = sendee.getGuildConfig().getMusicManager();
            TrackScheduler scheduler = manager.getScheduler();
            boolean extended = !flags.isEmpty();
            if(!manager.getScheduler().isPlaying()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry but you currently aren't playing anything.");
                return;
            }

            EmbedBuilder builder = Utils.getAudioTrackEmbed(manager.getPlayer().getPlayingTrack(), sendee);
            if(extended) {
                AudioTrack current = manager.getPlayer().getPlayingTrack();
                builder.addField("Position:", "`" + TimeUtil.format(manager.getPlayer().getTrackPosition())
                        + "/" + TimeUtil.format(current.getDuration()) + "`", true);
                builder.addField("Volume:", "`" + manager.getPlayer().getVolume() + "/150" + "`", true);
                builder.addField("QueueSize:", "`" + scheduler.getQueue().size() + "`", true);
                builder.addField("RepeatAll:", "`" + scheduler.isRepeatQueue() + "`", true);
                builder.addField("RepeatTrack:", "`" + scheduler.isRepeatTrack() + "`", true);
                builder.addField("Persist:", "`" + scheduler.isPersist() + "`", true);
            }
            sendee.sendMessage(ChannelTarget.MUSIC, builder.build());
        }
    }
}
