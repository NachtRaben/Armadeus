package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.Map;

public class AudioQueueListCommand extends Command {

    public AudioQueueListCommand() {
        super("queue", "", "Shows the current queue.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            if (manager.getScheduler().getCurrentTrack() == null) {
                sendee.sendMessage(ChannelTarget.GENERIC, "Can't show you an empty queue, add more tracks with `/play <search>`");
                return;
            }

            Member botMember = sendee.getGuild().getMember(sendee.getGuild().getJDA().getSelfUser());
            AudioTrack current = manager.getScheduler().getCurrentTrack();
            List<AudioTrack> tracks = manager.getScheduler().getQueue();
            long totalTime = current.getInfo().isStream ? 0 : current.getDuration();
            totalTime += tracks.stream().mapToLong(track -> track.getInfo().isStream ? 0 : track.getDuration()).sum();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("__**" + botMember.getEffectiveName() + "'s Queue:**__");
//            eb.setDescription("__***" + botMember.getEffectiveName() + "'s Queue:***__");
            eb.setColor(Utils.randomColor());
            eb.appendDescription("**[Current](" + current.getInfo().uri + ") ) " + current.getInfo().title + " for (`" +
                    (current.getInfo().isStream ? "Stream" : TimeUtil.fromLong(current.getDuration(), TimeUtil.FormatType.STRING))
                            + "`)**");
            int counter = 0;
            for (AudioTrack track : tracks) {
                if (counter++ >= 10)
                    break;
                String message = "\n**[" + counter + "](" + track.getInfo().uri + ") ) " + track.getInfo().title + " for (`"
                        + (track.getInfo().isStream ? "Stream" : TimeUtil.fromLong(track.getDuration(), TimeUtil.FormatType.STRING))
                        + "`)**";
                if(eb.getDescriptionBuilder().length() + message.length() > MessageEmbed.TEXT_MAX_LENGTH)
                    break;
                eb.appendDescription(message);
            }
            eb.setFooter("Tracks: " + tracks.size() + " Runtime: " + TimeUtil.fromLong(totalTime, TimeUtil.FormatType.STRING), sendee.getGuild().getJDA().getSelfUser().getAvatarUrl());
            sendee.sendMessage(ChannelTarget.MUSIC, eb.build());
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
