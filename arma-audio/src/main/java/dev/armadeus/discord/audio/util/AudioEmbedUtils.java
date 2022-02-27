package dev.armadeus.discord.audio.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.radio.Radio;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import static dev.armadeus.bot.api.util.EmbedUtils.newBuilder;

public class AudioEmbedUtils {

    public static MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer user, AudioTrack track) {
        if (track.getInfo().getTitle().contains("\u0000")) {
            Radio radio = Radio.getStation(track.getInfo().getTitle().split("\u0000")[0]);
            if (radio != null) {
                MessageEmbed embed = radio.getNowPlayingEmbed(user);
                if (embed != null) {
                    return embed;
                }
            }
        }
        EmbedBuilder builder = newBuilder(user);
        builder.setAuthor("Now Playing: ",
                        EmbedBuilder.URL_PATTERN.matcher(track.getInfo().getUri()).matches()
                                ? track.getInfo().getUri() : null, null)
                .setFooter("Requested by: " + user.getMember().getEffectiveName(), user.getUser().getAvatarUrl())
                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
                        track.getInfo().getTitle().contains("\u0000") ? track.getInfo().getTitle().split("\u0000")[1] : track.getInfo().getTitle(),
                        track.getInfo().getAuthor(),
                        track.getInfo().isStream() ? "Stream" : TimeUtil.format(track.getInfo().getLength())));
        if (track.getInfo().getUri().contains("youtube"))
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getInfo().getIdentifier()));
        return builder.build();
    }
}
