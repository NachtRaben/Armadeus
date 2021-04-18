package dev.armadeus.bot.commands.audio;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.TimeUtil;
import dev.armadeus.core.util.Utils;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
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
            GuildMusicManager manager = Armadeus.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
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
                    (current.getInfo().isStream ? "Stream" : TimeUtil.format(current.getDuration()))
                            + "`)**");
            int counter = 0;
            for (AudioTrack track : tracks) {
                if (counter++ >= 10)
                    break;
                String message = "\n**[" + counter + "](" + track.getInfo().uri + ") ) " + track.getInfo().title + " for (`"
                        + (track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getDuration()))
                        + "`)**";
                if(eb.getDescriptionBuilder().length() + message.length() > MessageEmbed.TEXT_MAX_LENGTH)
                    break;
                eb.appendDescription(message);
            }
            eb.setFooter("Tracks: " + tracks.size() + " Runtime: " + TimeUtil.format(totalTime), sendee.getGuild().getJDA().getSelfUser().getAvatarUrl());
            sendee.sendMessage(ChannelTarget.MUSIC, eb.build());
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
