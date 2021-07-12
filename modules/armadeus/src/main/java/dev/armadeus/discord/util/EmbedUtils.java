package dev.armadeus.discord.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtils {

    public static EmbedBuilder newBuilder(DiscordCommandIssuer user) {
        EmbedBuilder builder = new EmbedBuilder();
        if (user.isFromGuild()) {
            builder.setColor(user.getGuild().getSelfMember().getRoles().get(0).getColor());
        }
        return builder;
    }

//    public static EmbedBuilder getNowPlayingEmbed(DiscordCommandIssuer user, AudioTrack track) {
//        EmbedBuilder builder = newBuilder(user);
//        builder.setAuthor("Now Playing: ",
//                EmbedBuilder.URL_PATTERN.matcher(track.getInfo().uri).matches()
//                        ? track.getInfo().uri : null, null)
//                .setFooter("Requested by: " + user.getMember().getEffectiveName(), user.getUser().getAvatarUrl())
//                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
//                        track.getInfo().title,
//                        track.getInfo().author,
//                        track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getInfo().length)));
//        if (track instanceof YoutubeAudioTrack)
//            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
//        return builder;
//    }
}
