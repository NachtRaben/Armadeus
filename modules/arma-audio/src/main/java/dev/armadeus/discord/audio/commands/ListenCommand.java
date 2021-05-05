package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.StringUtils;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.AudioRequester;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.RichPresence;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ListenCommand extends AudioCommand {

    @Conditions("developeronly|guildonly")
    @CommandAlias("listen")
    public void listen(DiscordCommandIssuer user, @Default(value = "true") boolean listen) {
        if (cannotQueueMusic(user))
            return;

        AudioManager manager = getAudioManager(user);

        ScheduledTask future = manager.getListeners().get(user.getUser().getIdLong());
        if (future != null && !manager.getListeners().containsKey(user.getUser().getIdLong())) {
            user.sendMessage("Sorry, but I'm already listening to my Senpai~");
            return;
        } else if (future != null && !listen) {
            future.cancel();
            user.sendMessage("Sorry Senpai~, guess I'm not good enough to listen to you :c");
            manager.getListeners().remove(user.getUser().getIdLong());
            return;
        } else if (future != null) {
            user.sendMessage("Senpai~ I'm already listening to UwU");
            return;
        }

        manager.listeners.put(user.getUser().getIdLong(), ArmaCore.get().getScheduler().buildTask(ArmaAudio.get(), new ListenRunnable(user)).delay(1, TimeUnit.SECONDS).repeat(10, TimeUnit.SECONDS).schedule());
    }

    private static class SpotifyPresence {

        private RichPresence presence;

        public SpotifyPresence(DiscordCommandIssuer user) {
            Activity act = user.getMember().getActivities().stream().filter(a -> a.getName().equalsIgnoreCase("spotify")).findFirst().orElse(null);
            presence = act != null ? act.asRichPresence() : null;
            if (act == null || presence == null) {
                return;
            }
            if (presence.getTimestamps() == null) {
                act = null;
                presence = null;
            }
        }

        public String getTitle() {
            return presence.getDetails();
        }

        public String getAuthor() {
            return presence.getState();
        }

        public long getPosition() {
            return Objects.requireNonNull(presence.getTimestamps()).getElapsedTime(ChronoUnit.MILLIS);
        }

        public boolean isListening() {
            return presence != null;
        }
    }

    private class ListenRunnable implements Runnable {

        private DiscordCommandIssuer user;

        public ListenRunnable(DiscordCommandIssuer user) {
            this.user = user;
        }

        @Override
        public void run() {
            AudioManager manager = getAudioManager(user);
            long botChannel = manager.getLink().getChannelId();

            // Attempt a connection to get an exception
            if(botChannel == -1 && user.getVoiceChannel() != null) {
                if(!manager.getScheduler().joinVoiceChannel(user)) {
                    stop("Failed to join voice channel");
                }
            }

            // User left or moved channels
            if (user.getVoiceChannel() == null || (botChannel != -1 && user.getVoiceChannel().getIdLong() != botChannel)) {
                logger.warn("Stopped listening to {} because they left the channel", user.getMember().getEffectiveName());
                stop("you left the channel");
                return;
            }

            // User not listening to spotify
            SpotifyPresence presence = new SpotifyPresence(user);
            if (!presence.isListening()) {
                logger.warn("Stopped listening to {} because they closed spotify", user.getMember().getEffectiveName());
                stop("You are no longer listening to Spotify");
                return;
            }

            AudioTrack track = manager.getPlayer().getPlayingTrack();
            long position = track != null ? manager.getPlayer().getTrackPosition() : 0;
            boolean shouldSearch = (track == null || !StringUtils.tokenCompare(track.getInfo().title, presence.getTitle(), presence.getAuthor()));
            if (shouldSearch) {
                ListenCommand.this.logger.info("Changing track expected {} but got {}", presence.getTitle(), track == null ? "NULL" : track.getInfo().title);
                CompletableFuture<List<AudioTrack>> future = manager.getLink().getRestClient().getYoutubeSearchResult(presence.getTitle() + " " + presence.getAuthor());
                try {
                    List<AudioTrack> tracks = future.get(10, TimeUnit.SECONDS);

                    // Couldn't locate track
                    if (tracks.isEmpty()) {
                        logger.warn("Stopped listening to {} because no results found", user.getMember().getEffectiveName());
                        stop(String.format("I couldn't find any tracks for `%s %s`", presence.getTitle(), presence.getAuthor()));
                        return;
                    }

                    boolean found = false;
                    for (AudioTrack t : tracks) {
                        found = StringUtils.tokenCompare(t.getInfo().title, presence.getTitle(), presence.getAuthor());
                        if (!found)
                            continue;
                        t.setUserData(user);
                        manager.getScheduler().play(t);
                        manager.getPlayer().seekTo(presence.getPosition());
                        break;
                    }

                    // Couldn't match track
                    if (!found) {
                        stop(String.format("I couldn't match any tracks for `%s %s`", presence.getTitle(), presence.getAuthor()));
                        logger.warn("Stopped listening to {} because no matches found", user.getMember().getEffectiveName());
                    }
                } catch (Exception e) {
                    logger.warn("Exception encountered while loading tracks", e);
                    stop(e.getLocalizedMessage());
                }
            } else if (Math.abs(presence.getPosition() - position) > 5000) {
                ListenCommand.this.logger.info("We detected drift, expected {} but got {}", TimeUtil.format(presence.getPosition()), TimeUtil.format(position));
                manager.getPlayer().seekTo(presence.getPosition());
            }
        }

        private void stop(String message) {
            AudioManager manager = getAudioManager(user);
            ScheduledTask future = manager.listeners.get(user.getUser().getIdLong());
            if (future != null) {
                manager.listeners.remove(user.getUser().getIdLong());
                future.cancel();
            }
            user.sendMessage("I am no longer listening to you Senpai~ %s", message);
        }
    }
}
