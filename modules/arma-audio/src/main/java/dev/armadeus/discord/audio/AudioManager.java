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
import dev.armadeus.discord.audio.util.PlayerWrapper;
import lavalink.client.io.Link;
import lavalink.client.io.filters.Filters;
import lavalink.client.io.jda.JdaLink;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    @Getter
    private final Guild guild;
    private final CommentedConfig audioConfig;
    @Getter
    public Map<Long, ScheduledTask> listeners = new HashMap<>();
    float[] bands = new float[]{ 0.075f, 0.0375f, 0.03f, 0.022499999f, 0.0f, -0.015f, -0.022499999f, -0.0375f, -0.022499999f, -0.015f, 0.0f, 0.022499999f, 0.03f, 0.0375f, 0.075f };
    @Getter
    private PlayerWrapper player;

    public AudioManager(Guild guild) {
        this.guild = guild;
        // Load configurations
        GuildConfig config = ArmaAudio.core().guildManager().getConfigFor(guild);
        this.audioConfig = config.getMetadataOrInitialize("arma-audio", conf -> conf.set("volume", Float.toString(0.4f)));
        // Initialize Player
        this.player = new PlayerWrapper(this, ArmaAudio.get().getLavalink().getLink(guild).getPlayer());
        player.getLink().getNode(true);
        // Initialize Volume and Equalizer
        player.setVolume((int) (getVolume() * 100.0f)); // Bug in lavalink, this sets initial volume state since filters don't take effect initially
        Filters filters = player.getFilters();
        filters = filters.setVolume(getVolume());
        for (int i = 0; i < this.bands.length; i++) {
            filters = filters.setBand(i, this.bands[i] * 1.5f);
        }
        filters.commit();
        logger.info("Setting initial volume for {} to {}", guild.getName(), audioConfig.get("volume"));
    }

    private float getVolume() {
        // We parse as string for better config formatting
        return Float.parseFloat(audioConfig.get("volume"));
    }

    public TrackScheduler getScheduler() {
        return player.getScheduler();
    }

    public void setVolume(float vol) {
        vol = (float)Math.min(Math.max(vol, 0.0), 1.0);
        audioConfig.set("volume", Float.toString(vol));
        getPlayer().getFilters().setVolume(vol).commit();
    }

    public static class TrackLoader {
        public static void loadAndPlay(DiscordCommandIssuer user, String identifier, int limit) {
            boolean isSearch = identifier.startsWith("ytsearch:") || identifier.startsWith("scsearch:");
            if (isSearch) {
                processSearch(user, identifier, limit);
                return;
            }
            ArmaAudio.getManagerFor(user.getGuild()).getPlayer().getLink().getRestClient().loadItem(identifier, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {
                    TrackLoader.trackLoaded(user, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    TrackLoader.playlistLoaded(user, playlist, limit);
                }

                @Override
                public void noMatches() {
                    TrackLoader.loadAndPlay(user, "ytsearch:" + identifier, limit);
                }

                @Override
                public void loadFailed(FriendlyException e) {
                    user.sendMessage(String.format("Failed to load results because of `%s`", e.getMessage()));
                    logger.warn("Exception encountered during track loading for " + identifier, e);
                }
            });
        }

        private static void processSearch(DiscordCommandIssuer user, String search, int limit) {
            user.getChannel().sendTyping().queue();
            CompletableFuture<List<AudioTrack>> future;
            JdaLink link = ArmaAudio.getManagerFor(user.getGuild()).getPlayer().getLink();
            if (search.startsWith("ytsearch:")) {
                future = link.getRestClient().getYoutubeSearchResult(search.replaceFirst("ytsearch:", ""));
            } else if (search.startsWith("scsearch:")) {
                future = link.getRestClient().getSoundcloudSearchResult(search.replaceFirst("scsearch:", ""));
            } else {
                throw new IllegalArgumentException("Unknown search pattern: " + search);
            }
            future.thenAccept(tracks -> {
                if (tracks.isEmpty()) {
                    user.sendMessage("No results found for `" + search.substring(search.indexOf(':') + 1) + "`");
                    return;
                }
                AudioPlaylist playlist = new BasicAudioPlaylist("Search Results for " + search.substring(search.indexOf(':') + 1), tracks, tracks.get(0), true);
                TrackLoader.playlistLoaded(user, playlist, limit);
            });
        }

        private static void trackLoaded(DiscordCommandIssuer user, AudioTrack track) {
            track.setUserData(user);
            ArmaAudio.getManagerFor(user.getGuild()).getPlayer().getScheduler().queue(track);
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
                ArmaAudio.getManagerFor(user.getGuild()).getPlayer().getScheduler().queue(track);
            }
        }
    }

}
