package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.core.audio.TrackScheduler;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.EmbedUtils;
import dev.armadeus.core.util.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;

public class NowPlayingCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("np|playing")
    public void nowPlaying(DiscordUser user, @Default(value = "false") boolean extended) {
        if (isNotPlaying(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();
        TrackScheduler scheduler = manager.getScheduler();
        EmbedBuilder eb = EmbedUtils.getNowPlayingEmbed(user, manager.getPlayer().getPlayingTrack());
        if (extended) {
            AudioTrack current = manager.getPlayer().getPlayingTrack();
            eb.addField("Position:", "`" + TimeUtil.format(manager.getPlayer().getTrackPosition())
                    + "/" + TimeUtil.format(current.getDuration()) + "`", true);
            eb.addField("Volume:", "`" + manager.getPlayer().getFilters().getVolume() * 100 + "%" + "`", true);
            eb.addField("QueueSize:", "`" + scheduler.getQueue().size() + "`", true);
            eb.addField("RepeatAll:", "`" + scheduler.isRepeatQueue() + "`", true);
            eb.addField("RepeatTrack:", "`" + scheduler.isRepeatTrack() + "`", true);
            eb.addField("Persist:", "`" + scheduler.isPersist() + "`", true);
        }
        user.sendPrivateMessage(eb.build());
    }
}
