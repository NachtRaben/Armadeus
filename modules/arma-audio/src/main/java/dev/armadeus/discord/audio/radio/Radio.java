package dev.armadeus.discord.audio.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.radio.stations.Hive365;
import dev.armadeus.discord.audio.util.AudioInfoModifier;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public abstract class Radio {

    private static final Map<String, Radio> STATIONS = new HashMap<>();
    private static final Hive365 HIVE_365 = new Hive365();

    protected final String identifier;
    protected final String title;
    protected final String artist;
    protected final String url;

    public Radio(String identifier, String title, String artist, String url) {
        this.identifier = identifier;
        this.title = title;
        this.artist = artist;
        this.url = url;
        STATIONS.put(identifier.toLowerCase(Locale.ROOT), this);
    }

    public static Radio getStation(String station) {
        return STATIONS.get(station.toLowerCase(Locale.ROOT));
    }

    public MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer issuer) { return null; }

    public void play(DiscordCommandIssuer user) {
        AudioManager manager = ArmaAudio.getManagerFor(user.getGuild());
        manager.getPlayer().getLink().getRestClient().loadItem(url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(user);
                AudioInfoModifier info = new AudioInfoModifier(track.getInfo());
                info.setTitle(identifier + "\u0000" + title).setArtist(artist);
                manager.getScheduler().play(track);
                manager.getScheduler().setRepeatTrack(true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void noMatches() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                user.sendMessage("Failed to play `" +  identifier + " Radio`, is it offline?");
            }
        });
    }

}
