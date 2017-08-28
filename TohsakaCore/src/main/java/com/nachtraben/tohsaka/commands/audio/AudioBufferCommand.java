package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Arrays;
import java.util.Map;

public class AudioBufferCommand extends Command {

    public AudioBufferCommand() {
        super("buffer", "<time>", "Buffers/Scans to a certain point in the current track.");
        setAliases(Arrays.asList("scan"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            long time = Math.abs(TimeUtil.stringToMillis(args.get("time")));
            AudioTrack currTrack = manager.getScheduler().getCurrentTrack();
            if(!manager.getScheduler().isPlaying()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Gotta play something to buffer, add more tracks with `/play <search>`");
                return;
            }
            if(currTrack.getInfo().isStream) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but I can't buffer a stream.");
                return;
            }
            if(time >= currTrack.getDuration()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but I can only buffer to `" + TimeUtil.millisToString(currTrack.getDuration(), TimeUtil.FormatType.STRING) + "`.");
                return;
            }
            sendee.sendMessage(ChannelTarget.MUSIC, "Buffering to `" + TimeUtil.millisToString(time, TimeUtil.FormatType.STRING) + "`.");
            manager.getPlayer().getPlayingTrack().setPosition(time);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
