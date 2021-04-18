package dev.armadeus.bot.commands.moderation;

import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.configuration.BotConfig;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
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
            BotConfig config = BotConfig.get();
            if (!config.getDeveloperIds().contains(sendee.getUser().getIdLong()) && !config.getOwnerIds().contains(sendee.getUser().getIdLong())) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but that feature isn't available to everyone.");
                return;
            }
            GuildMusicManager from = sendee.getBot().getGuildManager().getConfigurationFor(sendee.getBot().getShardManager().getGuildById(Long.parseLong(args.get("guildid")))).getMusicManager(false);
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
