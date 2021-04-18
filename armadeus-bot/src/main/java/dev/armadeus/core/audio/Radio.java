package dev.armadeus.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum Radio {

    // Commented unless in dev.
    SPOTIFY("Spotify Radio", "NachtRaben", "http://localhost:13375", null),
    HIVE("Hive Radio", "Hive365", "http://stream.hive365.co.uk:8088/live",
            new EmbedBuilder().setColor(Color.YELLOW)
                    .setTitle("Radio provided by Hive365.", "https://hive365.co.uk/")
                    .setDescription("Non-stop music guaranteed to get ya buzzin'")
                    .build()),
    MOE("LISTEN.moe", "LISTEN.moe", "https://listen.moe/m3u8/jpop.m3u", null),
    ROOKERY("Rookery Radio", "Real College Radio", "http://stream.rookeryradio.com:8088/live", null),
    NOLIFE("NoLife-Radio", "NoLife", "http://nolife-radio.com/radio/NoLife-radio.m3u", null);

    private static final Logger logger = LogManager.getLogger();
    private static Field titleField;
    private static Field artistField;

    static {
        try {
            Class clz = AudioTrackInfo.class;
            titleField = clz.getDeclaredField("title");
            titleField.setAccessible(true);
            artistField = clz.getDeclaredField("author");
            artistField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            modifiersField.setInt(titleField, titleField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(artistField, artistField.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn("Failed to modify AudioTrackInfo fields!", e);
        }
    }

    private final String title;
    private final String artist;
    private final String url;
    private final MessageEmbed embed;

    Radio(String title, String artist, String url, MessageEmbed embed) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.embed = embed;
    }

    public void playRadio(DiscordUser sender) {
        GuildMusicManager manager = sender.getGuildConfig().getMusicManager();
        manager.getLink().getRestClient().loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(sender);
                modifyTrackInfo(track.getInfo(), title, artist);
                if (embed != null)
                    sender.sendMessage(embed);
                manager.getScheduler().play(track);
                manager.getScheduler().setRepeatTrack(true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                logger.debug("Somehow loaded a playlist when trying to play, " + this);
                sender.sendMessage("Something went wrong while playing the radio, the bot author has been notified. FOUND_PLAYLIST");
            }

            @Override
            public void noMatches() {
                logger.debug("Couldn't load radio when attempt to play, " + this);
                sender.sendMessage("Something went wrong while playing the radio, the bot author has been notified. NO_MATCH");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sender.sendMessage("Failed to play `" + name() + " Radio`, is it offline?");
            }
        });
    }

    public static Radio forName(String s) {
        for(Radio r : values()) {
            if(s.equalsIgnoreCase(r.toString()))
                return r;
        }
        return null;
    }

    private void modifyTrackInfo(AudioTrackInfo info, String title, String artist) {
        try {
            if (titleField != null)
                titleField.set(info, title);
            if (artistField != null)
                artistField.set(info, artist);
        } catch (IllegalAccessException e) {
            logger.warn("Failed to modify radio track info.", e);
        }

    }


    @Override
    public String toString() {
        return name();
    }

    public static Radio getByAddress(String uri) {
        for (Radio rad : values())
            if (rad.url.equalsIgnoreCase(uri))
                return rad;
        return null;
    }
}
