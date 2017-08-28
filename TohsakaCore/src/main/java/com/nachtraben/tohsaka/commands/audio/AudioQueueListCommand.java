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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

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
            if(manager.getScheduler().getCurrentTrack() == null || manager.getScheduler().getQueue().isEmpty()) {
                sender.sendMessage("Can't show you an empty queue, add more tracks with `/play <search>`");
                return;
            }

            Member botMember = sendee.getGuild().getMember(sendee.getGuild().getJDA().getSelfUser());
            AudioTrack current = manager.getScheduler().getCurrentTrack();
            List<AudioTrack> tracks = manager.getScheduler().getQueue();
            long totalTime = current.getDuration();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setDescription("__***" + botMember.getEffectiveName() + "'s Queue:***__");
            eb.setColor(Utils.randomColor());
            eb.addField("Current ) " + current.getInfo().title, "by __*" + current.getInfo().author + "*__", false);
            int counter = 0;
            for(AudioTrack track : tracks) {
                if (counter >= 20)
                    break;
                Field f = new Field(++counter + ") " + track.getInfo().title,"by __*" + track.getInfo().author + "*__", false);
                eb.addField(f);
                try {
                    if(Utils.getEmbedLength(eb) >= MessageEmbed.EMBED_MAX_LENGTH_BOT - 250) {
                        eb.getFields().remove(f);
                        break;
                    }
                } catch (Exception e) {
                    eb.getFields().remove(f);
                    break;
                }
                totalTime += track.getDuration();
            }
            eb.setFooter("Tracks: " + tracks.size() + " Runtime: " + TimeUtil.millisToString(totalTime, TimeUtil.FormatType.STRING), sendee.getGuild().getJDA().getSelfUser().getAvatarUrl());
            sendee.sendMessage(ChannelTarget.MUSIC, eb.build());
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
