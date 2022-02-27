package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class QueueCommand extends AudioCommand {

    private static final String trackDescription = "\n**[%s](%s)** ) `%s` for (`%s`)";


    @Conditions("guildonly")
    @CommandAlias("queue")
    @CommandPermission("armadeus.queue")
    @Description("Shows all songs currently in queue")
    public void queue(DiscordCommandIssuer user) {
        AudioManager manager = getAudioManager(user);
        if (isNotPlaying(user))
            return;

        AudioTrack current = manager.getPlayer().getPlayingTrack();
        List<AudioTrack> tracks = manager.getPlayer().getScheduler().getQueue();
        long totalTime = current.getInfo().isStream() ? 0 : current.getInfo().getLength() - manager.getPlayer().getTrackPosition();
        totalTime += tracks.stream().mapToLong(track -> track.getInfo().isStream() ? 0 : track.getInfo().getLength()).sum();

        EmbedBuilder eb = new EmbedBuilder(AudioEmbedUtils.getNowPlayingEmbed(user, current));
        eb.setAuthor(user.getGuild().getSelfMember().getEffectiveName() + "'s Queue:",
                EmbedBuilder.URL_PATTERN.matcher(current.getInfo().getUri()).matches() ? current.getInfo().getUri() : null,
                null);

        eb.getDescriptionBuilder().insert(0, "**Currently Playing:** ");
        eb.appendDescription("\n\n**Queued:**\n");
        int counter = 0;
        for (AudioTrack track : tracks) {
            if (counter++ >= 10)
                break;

            String message = String.format(trackDescription, counter, track.getInfo().getUri(), track.getInfo().getTitle(), (track.getInfo().isStream() ? "Stream" : TimeUtil.format(track.getInfo().getLength())));
            if (eb.getDescriptionBuilder().length() + message.length() > MessageEmbed.TEXT_MAX_LENGTH)
                break;
            else
                eb.appendDescription(message);
        }
        eb.setFooter("Tracks: " + (tracks.size() + 1) + " Runtime: " + TimeUtil.format(totalTime), user.getJda().getSelfUser().getAvatarUrl());
        user.sendMessage(eb.build());
    }
}
