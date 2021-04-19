package dev.armadeus.core.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.audio.TrackScheduler;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.configuration.GuildConfig;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GuildMusicManager {

    private static final Logger logger = LogManager.getLogger();
    private final Guild guild;
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;
    public Map<Long, Future<?>> listeners = new HashMap<>();
    float[] bands = new float[]{ 0.075f, 0.0375f, 0.03f, 0.022499999f, 0.0f, -0.015f, -0.022499999f, -0.0375f, -0.022499999f, -0.015f, 0.0f, 0.022499999f, 0.03f, 0.0375f, 0.075f };

    public GuildMusicManager(Guild guild) {
        checkNotNull(guild);
        this.guild = guild;
        this.link = DiscordBot.get().getLavalink().getLink(guild);
        this.player = link.getPlayer();
        GuildConfig config = DiscordBot.get().getGuildManager().getConfigurationFor(guild);
        this.player.getFilters().setVolume(config.getVolume()).commit();
        for (int i = 0; i < this.bands.length; i++) {
            this.player.getFilters().setBand(i, this.bands[i]).commit();
        }
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
    }

    public void loadAndPlay(DiscordUser user, String identifier, int limit) {
        boolean isSearch = identifier.startsWith("ytsearch:") || identifier.startsWith("scsearch:");
        if (isSearch) {
            processSearch(user, identifier, limit);
            return;
        }
        getLink().getRestClient().loadItem(identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                GuildMusicManager.trackLoaded(user, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                GuildMusicManager.playlistLoaded(user, playlist, limit);
            }

            @Override
            public void noMatches() {
                loadAndPlay(user, "ytsearch:" + identifier, limit);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                user.sendMessage(String.format("Failed to load results because of `%s`", e.getMessage()));
                logger.warn("Exception encountered during track loading for " + identifier, e);
            }
        });
    }

    private void processSearch(DiscordUser user, String search, int limit) {
        user.getChannel().sendTyping().queue();
        CompletableFuture<List<AudioTrack>> future;
        if (search.startsWith("ytsearch:")) {
            future = getLink().getRestClient().getYoutubeSearchResult(search.replaceFirst("ytsearch:", ""));
        } else if (search.startsWith("scsearch:")) {
            future = getLink().getRestClient().getSoundcloudSearchResult(search.replaceFirst("scsearch:", ""));
        } else {
            throw new IllegalArgumentException("Unknown search pattern: " + search);
        }
        future.thenAccept(tracks -> {
            if (tracks.isEmpty()) {
                user.sendMessage("No results found for `" + search.substring(search.indexOf(':')) + "`");
                return;
            }
            AudioPlaylist playlist = new BasicAudioPlaylist("Search Results for " + search.substring(search.indexOf(':')), tracks, tracks.get(0), true);
            playlistLoaded(user, playlist, limit);
        });
    }

    private static void trackLoaded(DiscordUser user, AudioTrack track) {
        user.sendMessage(String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
        track.setUserData(user);
        user.getGuildConfig().getMusicManager().getScheduler().queue(track);
    }

    public static void playlistLoaded(DiscordUser user, AudioPlaylist playlist, int limit) {
        int start = playlist.getSelectedTrack() != null ? playlist.getTracks().indexOf(playlist.getSelectedTrack()) : 0; // Index of the starting track
        int available = playlist.getTracks().size() - start; // Songs available after the starting track
        int end = limit > 0 ? Math.min(limit, available) : available;
        int loaded = 0;
        for (int i = 0; i < end; i++) {
            AudioTrack track = playlist.getTracks().get(start + i);
            if (track == null) {
                break;
            }
            track.setUserData(user);
            user.getGuildConfig().getMusicManager().getScheduler().queue(track);
            loaded++;
        }
        if (limit > 1) {
            user.sendMessage(String.format("Adding `%s` tracks to the queue from `%s`. :3.", loaded, playlist.getName()));
        } else {
            AudioTrack track = playlist.getTracks().get(0);
            user.sendMessage(String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
        }
    }
}
