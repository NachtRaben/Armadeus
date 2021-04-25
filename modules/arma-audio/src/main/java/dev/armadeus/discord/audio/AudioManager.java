package dev.armadeus.discord.audio;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import lavalink.client.io.filters.Filters;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public class AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    private final GuildConfig config;
    private final CommentedConfig audioConfig;
    public Map<Long, ScheduledTask> listeners = new HashMap<Long, com.velocitypowered.api.scheduler.ScheduledTask>();
    float[] bands = new float[]{ 0.075f, 0.0375f, 0.03f, 0.022499999f, 0.0f, -0.015f, -0.022499999f, -0.0375f, -0.022499999f, -0.015f, 0.0f, 0.022499999f, 0.03f, 0.0375f, 0.075f };
    private TrackScheduler scheduler;
    private LavalinkPlayer player;

    private float getVolume() {
        return audioConfig.get("volume");
    }

    public AudioManager(Guild guild) {
        this.config = ArmaCore.get().getGuildManager().getConfigFor(guild);
        this.audioConfig = config.getMetadataOrInitialize("arma-audio", conf -> conf.set("volume", 1.0f));
        this.player = getLink().getPlayer();
        logger.info("Setting resume vol for {} to {}", guild.getName(), audioConfig.get("volume"));
        player.getLink().getNode(true);
        Filters filters = player.getFilters();
        filters = filters.setVolume(getVolume());
        for (int i = 0; i < this.bands.length; i++) {
            filters = filters.setBand(i, this.bands[i]);
        }
        filters.commit();
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
    }

    public JdaLink getLink() {
        return ArmaAudio.get().getLavalink().getLink(config.getGuild());
    }

    private static void trackLoaded(DiscordCommandIssuer user, AudioTrack track) {
        track.setUserData(user);
        ArmaAudio.getManagerFor(user.getGuild()).getScheduler().queue(track);
    }

    public static void playlistLoaded(DiscordCommandIssuer user, AudioPlaylist playlist, int limit) {
        int start = playlist.getSelectedTrack() != null ? playlist.getTracks().indexOf(playlist.getSelectedTrack()) : 0; // Index of the starting track
        int available = playlist.getTracks().size() - start; // Songs available after the starting track
        int end = limit > 0 ? Math.min(limit, available) : available;
        for (int i = 0; i < end; i++) {
            AudioTrack track = playlist.getTracks().get(start + i);
            if (track == null) {
                break;
            }
            track.setUserData(user);
            ArmaAudio.getManagerFor(user.getGuild()).getScheduler().queue(track);
        }
    }

    public void setVolume(float vol) {
        vol = Math.min(1.0f, vol);
        audioConfig.set("volume", vol);
        getPlayer().getFilters().setVolume(vol).commit();
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public void loadAndPlay(DiscordCommandIssuer user, String identifier, int limit) {
        boolean isSearch = identifier.startsWith("ytsearch:") || identifier.startsWith("scsearch:");
        if (isSearch) {
            processSearch(user, identifier, limit);
            return;
        }
        getLink().getRestClient().loadItem(identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                AudioManager.trackLoaded(user, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioManager.playlistLoaded(user, playlist, limit);
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

    private void processSearch(DiscordCommandIssuer user, String search, int limit) {
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
}
