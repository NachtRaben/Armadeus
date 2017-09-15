package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueueTransfer extends Command {

    public QueueTransfer() {
        super("qt", "<guildid>", "Transfers queue from one guild to another.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        System.err.println("Called.");
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            BotConfig config = sendee.getDbot().getConfig();
            if(!config.getDeveloperIDs().contains(sendee.getUserID()) && !config.getOwnerIDs().contains(sendee.getUserID())) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but that feature isn't available to everyone.");
                return;
            }
            GuildMusicManager from = sendee.getDbot().getGuildManager().getConfigurationFor(sendee.getDbot().getShardManager().getGuildByID(Long.parseLong(args.get("guildid")))).getMusicManager(false);
            GuildMusicManager to = sendee.getGuildConfig().getMusicManager();
            if(from == null || !from.getScheduler().isPlaying()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Can't transfer from a guild that isn't playing anything.");
                return;
            }

            if(!to.getScheduler().isPlaying() && !sendee.getMember().getVoiceState().inVoiceChannel()) {
                sendee.sendMessage(ChannelTarget.MUSIC, "You have to be in a voice channel to transfer a queue.");
                return;
            }

            List<AudioTrack> newTracks = new ArrayList<>();
            AudioTrack currTrack = from.getScheduler().getCurrentTrack();
            currTrack.setUserData(sendee);
            newTracks.add(currTrack);

            for(AudioTrack track : from.getScheduler().getQueue()) {
                AudioTrack t = track.makeClone();
                t.setUserData(sendee);
                newTracks.add(t);
            }

            newTracks.forEach(track -> to.getScheduler().queue(track));
            sendee.sendMessage(ChannelTarget.MUSIC, "Transferred `" + newTracks.size() + "` tracks from `" + from.getGuild().getName() + "`.");
        }
    }
}
