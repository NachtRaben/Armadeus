package dev.armadeus.core.util;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.core.command.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtils {

    public static EmbedBuilder newBuilder(DiscordUser user) {
        EmbedBuilder builder = new EmbedBuilder();
        if (user.isFromGuild()) {
            builder.setColor(user.getGuild().getSelfMember().getRoles().get(0).getColor());
        } else {
            builder.setColor(Utils.randomColor());
        }
        return builder;
    }

    public static EmbedBuilder getNowPlayingEmbed(DiscordUser user, AudioTrack track) {
        EmbedBuilder builder = newBuilder(user);
        builder.setAuthor("Now Playing: ",
                EmbedBuilder.URL_PATTERN.matcher(track.getInfo().uri).matches()
                        ? track.getInfo().uri : null, null)
                .setFooter("Requested by: " + user.getMember().getEffectiveName(), user.getUser().getAvatarUrl())
                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
                        track.getInfo().title,
                        track.getInfo().author,
                        track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getInfo().length)));
        if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
        return builder;
    }
}
