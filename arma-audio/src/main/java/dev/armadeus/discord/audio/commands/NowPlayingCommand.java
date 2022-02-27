package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.TrackScheduler;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;

public class NowPlayingCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("np|playing")
    @CommandPermission("armadeus.playing")
    @Description("Shows the currently playing track")
    public void nowPlaying(DiscordCommandIssuer user, @Default(value = "false") boolean extended) {
        if (isNotPlaying(user))
            return;

        AudioManager manager = getAudioManager(user);
        TrackScheduler scheduler = manager.getPlayer().getScheduler();
        EmbedBuilder eb = new EmbedBuilder(AudioEmbedUtils.getNowPlayingEmbed(user, manager.getPlayer().getPlayingTrack()));
        if (extended) {
            AudioTrack current = manager.getPlayer().getPlayingTrack();
            long position = current != null ? manager.getPlayer().getInternalPlayer().getTrackPosition() : 0;
            assert current != null;
            eb.addField("Position:", "`" + TimeUtil.format(manager.getPlayer().getTrackPosition())
                    + "/" + (!current.getInfo().isStream() ? TimeUtil.format(current.getInfo().getLength()) : "unknown") + "`", true);
            eb.addField("Volume:", "`" + manager.getPlayer().getFilters().getVolume() * 100 + "%" + "`", true);
            eb.addField("QueueSize:", "`" + scheduler.getQueue().size() + "`", true);
            eb.addField("RepeatAll:", "`" + scheduler.isRepeatQueue() + "`", true);
            eb.addField("RepeatTrack:", "`" + scheduler.isRepeatTrack() + "`", true);
        }
        user.sendMessage(eb.build());
    }
}
