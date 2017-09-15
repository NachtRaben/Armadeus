package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;

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
                builder.addField("Position:", "`" + TimeUtil.millisToString(current.getPosition(), TimeUtil.FormatType.STRING)
                        + "/" + TimeUtil.millisToString(current.getDuration(), TimeUtil.FormatType.STRING) + "`" , true);
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
