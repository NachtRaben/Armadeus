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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AudioManager {

    @Getter
    private final Guild guild;
    @Getter
    private final CommentedConfig audioConfig;
    @Getter
    public Map<Long, ScheduledTask> listeners = new HashMap<>();
    private PlayerWrapper player;
    float[] bands = new float[]{ 0.075f, 0.0375f, 0.03f, 0.022499999f, 0.0f, -0.015f, -0.022499999f, -0.0375f, -0.022499999f, -0.015f, 0.0f, 0.022499999f, 0.03f, 0.0375f, 0.075f };

    public AudioManager(Guild guild) {
        this.guild = guild;
        // Load configurations
        GuildConfig config = ArmaAudio.core().guildManager().getConfigFor(guild);
        this.audioConfig = config.getMetadataOrInitialize("arma-audio", conf -> conf.set("volume", Float.toString(0.4f)));
        ArmaAudio.core().scheduler().buildTask(ArmaAudio.get(), () -> {
            if(!getScheduler().isPlaying() && getPlayer().getLink().getChannelId() != -1) {
                log.warn("{} => Disconnecting due to inactivity", guild.getName());
                getScheduler().stop();
                getPlayer().getLink().destroy();
            }
        }).repeat(5, TimeUnit.MINUTES).schedule();
    }

    public float getVolume() {
        // We parse as string for better config formatting
        return (float) Math.min(Math.max(Float.parseFloat(audioConfig.get("volume")), 0.0), 5.0);
    }

    public TrackScheduler getScheduler() {
        return player.getScheduler();
    }

    public PlayerWrapper getPlayer() {
        if(player == null || player.getLink().getState().equals(Link.State.DESTROYED)) {
            log.warn("Creating new player for {}", getGuild().getName());
            this.player = new PlayerWrapper(this, ArmaAudio.get().getLavalink().getLink(guild).getPlayer());
            player.getLink().getNode(true);
            player.setVolume(getVolume());
            Filters filters = player.getFilters();
            for (int i = 0; i < this.bands.length; i++) {
                filters = filters.setBand(i, this.bands[i] * 1.5f);
            }
            filters.commit();
        }
        return player;
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
                    log.warn("Exception encountered during track loading for " + identifier, e);
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
                if(limit > 1) {
                    AudioPlaylist playlist = new BasicAudioPlaylist("Search Results for " + search.substring(search.indexOf(':') + 1), tracks, tracks.get(0), true);
                    TrackLoader.playlistLoaded(user, playlist, limit);
                } else {
                    TrackLoader.trackLoaded(user, tracks.get(0));
                }
            });
        }

        private static void trackLoaded(DiscordCommandIssuer user, AudioTrack track) {
            track.setUserData(user);
            ArmaAudio.getManagerFor(user.getGuild()).getPlayer().getScheduler().queue(track);
            user.sendMessage(String.format("Added `%s` by `%s` to the queue!", track.getInfo().title, track.getInfo().author), 5);
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
            user.sendMessage(String.format("Added `%s` songs to the queue!", (end - start)), 5);
        }
    }

}
