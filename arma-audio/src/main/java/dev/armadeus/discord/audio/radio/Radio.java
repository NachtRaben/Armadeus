package dev.armadeus.discord.audio.radio;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.radio.stations.Hive365;
import dev.armadeus.discord.audio.radio.stations.ListenMoe;
import dev.armadeus.discord.audio.radio.stations.NoLife;
import dev.armadeus.discord.audio.util.AudioInfoModifier;
import lavalink.client.io.FunctionalResultHandler;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class Radio {

    @Getter
    private static Map<String, Radio> stations = new HashMap<>();
    private static final Hive365 HIVE_365 = new Hive365();
    private static final NoLife NO_LIFE = new NoLife();
    private static final ListenMoe LISTEN_MOE = new ListenMoe();
    protected static final OkHttpClient CLIENT = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String identifier;
    protected final String title;
    protected final String artist;
    protected final String url;

    public Radio(String identifier, String title, String artist, String url) {
        this.identifier = identifier;
        this.title = title;
        this.artist = artist;
        this.url = url;
        stations.put(identifier.toLowerCase(Locale.ROOT), this);
    }

    public static Radio getStation(String station) {
        return stations.get(station.toLowerCase(Locale.ROOT));
    }

    public MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer issuer) {
        return null;
    }

    public void play(DiscordCommandIssuer user) {
        log.warn("Playing radio");
        AudioManager manager = ArmaAudio.getManagerFor(user.getGuild());
        manager.getPlayer().getLink().getRestClient().loadItem(url, new FunctionalResultHandler(
                        track -> {
                            track.setUserData(user);
                            AudioInfoModifier info = new AudioInfoModifier(track.getInfo());
                            info.setTitle(identifier + "\u0000" + title).setArtist(artist);
                            manager.getScheduler().play(track);
                            manager.getScheduler().setRepeatTrack(true);
                        },
                        playlist -> {
                            throw new UnsupportedOperationException();
                        },
                        search -> {
                            throw new UnsupportedOperationException();
                        },
                        () -> {
                        },
                        exception -> {
                            user.sendMessage("Failed to play `" + identifier + " Radio`, is it offline?");
                            exception.printStackTrace();
                        }

                )
        );
    }
}
