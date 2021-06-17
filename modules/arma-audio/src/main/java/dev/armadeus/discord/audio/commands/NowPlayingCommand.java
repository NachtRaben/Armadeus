package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.TrackScheduler;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class NowPlayingCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("np|playing")
    public void nowPlaying(DiscordCommandIssuer user, @Default(value = "false") boolean extended) {
        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);
        TrackScheduler scheduler = manager.getPlayer().getScheduler();
        EmbedBuilder eb = new EmbedBuilder(AudioEmbedUtils.getNowPlayingEmbed(user, manager.getPlayer().getPlayingTrack()));
        if (extended) {
            AudioTrack current = manager.getPlayer().getPlayingTrack();
            eb.addField("Position:", "`" + TimeUtil.format(manager.getPlayer().getTrackPosition())
                    + "/" + TimeUtil.format(current.getDuration()) + "`", true);
            eb.addField("Volume:", "`" + manager.getPlayer().getFilters().getVolume() * 100 + "%" + "`", true);
            eb.addField("QueueSize:", "`" + scheduler.getQueue().size() + "`", true);
            eb.addField("RepeatAll:", "`" + scheduler.isRepeatQueue() + "`", true);
            eb.addField("RepeatTrack:", "`" + scheduler.isRepeatTrack() + "`", true);
        }
        user.sendPrivateMessage(eb.build());
    }
}
