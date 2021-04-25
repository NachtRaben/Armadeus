package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class QueueShowCommand extends AudioCommand {

    private static final String trackDescription = "\n**[%s](%s)** ) `%s` for (`%s`)";


    @Conditions("guildonly")
    @CommandAlias("queue")
    public void queue(DiscordCommandIssuer user) {
        AudioManager manager = getAudioManager(user);
        if (isNotPlaying(user))
            return;

        AudioTrack current = manager.getPlayer().getPlayingTrack();
        List<AudioTrack> tracks = manager.getScheduler().getQueue();
        long totalTime = current.getInfo().isStream ? 0 : current.getDuration();
        totalTime += tracks.stream().mapToLong(track -> track.getInfo().isStream ? 0 : track.getDuration()).sum() + current.getDuration() - current.getPosition();

        EmbedBuilder eb = AudioEmbedUtils.getNowPlayingEmbed(user, current);
        eb.setAuthor(user.getGuild().getSelfMember().getEffectiveName() + "'s Queue:",
                EmbedBuilder.URL_PATTERN.matcher(current.getInfo().uri).matches() ? current.getInfo().uri : null,
                null);

        eb.getDescriptionBuilder().insert(0, "**Currently Playing:**");
        eb.appendDescription("\n\n**Queued:**\n");
        int counter = 0;
        for (AudioTrack track : tracks) {
            if (counter++ >= 10)
                break;

            String message = String.format(trackDescription, counter, track.getInfo().uri, track.getInfo().title, (track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getDuration())));
            if (eb.getDescriptionBuilder().length() + message.length() > MessageEmbed.TEXT_MAX_LENGTH)
                break;
            else
                eb.appendDescription(message);
        }
        eb.setFooter("Tracks: " + (tracks.size() + 1) + " Runtime: " + TimeUtil.format(totalTime), user.getJda().getSelfUser().getAvatarUrl());
        user.sendMessage(eb.build());
    }
}
