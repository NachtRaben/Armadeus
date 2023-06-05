package dev.armadeus.discord.audio.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import static dev.armadeus.bot.api.util.EmbedUtils.newBuilder;

public class AudioEmbedUtils {

    public static MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer user, AudioTrack track) {
        return getNowPlayingEmbed(user.getMember(), track);
    }

    public static MessageEmbed getNowPlayingEmbed(Member user, AudioTrack track) {
        if (track.getInfo().title.contains("\u0000")) {
            Radio radio = Radio.getStation(track.getInfo().title.split("\u0000")[0]);
            if (radio != null) {
                MessageEmbed embed = radio.getNowPlayingEmbed(user);
                if (embed != null) {
                    return embed;
                }
            }
        }
        EmbedBuilder builder = newBuilder(user);
        builder.setAuthor("Now Playing: ",
                        EmbedBuilder.URL_PATTERN.matcher(track.getInfo().uri).matches()
                                ? track.getInfo().uri : null, null)
                .setFooter("Requested by: " + user.getEffectiveName(), user.getUser().getAvatarUrl())
                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
                        track.getInfo().title.contains("\u0000") ? track.getInfo().title.split("\u0000")[1] : track.getInfo().title,
                        track.getInfo().author,
                        track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getInfo().length)));
        if (track.getInfo().uri.contains("youtube"))
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getInfo().identifier));
        return builder.build();
    }
}
