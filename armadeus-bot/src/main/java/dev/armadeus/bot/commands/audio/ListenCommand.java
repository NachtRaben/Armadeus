package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.TimeUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.RichPresence;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.armadeus.bot.util.StringUtils.tokenCompare;
import static dev.armadeus.core.util.Utils.EXEC;

public class ListenCommand extends AudioCommand {

    @Conditions("developeronly|guildonly")
    @CommandAlias("listen")
    public void listen(DiscordUser user, @Default(value = "true") boolean listen) {
        if (cannotQueueMusic(user))
            return;

        GuildMusicManager manager = user.getGuildMusicManager();

        Future<?> future = manager.getListeners().get(user.getUser().getIdLong());
        if (future != null && !manager.getListeners().containsKey(user.getUser().getIdLong())) {
            user.sendMessage("Sorry, but I'm already listening to my Senpai~");
            return;
        } else if (future != null && !listen) {
            future.cancel(true);
            user.sendMessage("Sorry Senpai~, guess I'm not good enough to listen to you :c");
            return;
        } else if (future != null) {
            user.sendMessage("Senpai~ I'm already listening to UwU");
            return;
        }

        manager.listeners.put(user.getUser().getIdLong(), EXEC.submit(new ListenRunnable(user)));
        user.sendMessage("Now listening to you Senpai~ c:");
    }

    private static class SpotifyPresence {

        private RichPresence presence;

        public SpotifyPresence(DiscordUser user) {
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

        AtomicBoolean error = new AtomicBoolean(false);
        DiscordUser user;

        public ListenRunnable(DiscordUser user) {
            this.user = user;
        }

        @Override
        public void run() {
            GuildMusicManager manager = user.getGuildMusicManager();
            while (!Thread.interrupted() && !error.get()) {
                long botChannel = manager.getLink().getChannelId();
                if (user.getVoiceChannel() == null || (botChannel != -1 && user.getVoiceChannel().getIdLong() != botChannel)) {
                    break;
                }

                SpotifyPresence presence = new SpotifyPresence(user);
                if (!presence.isListening()) {
                    break;
                }

                AudioTrack track = manager.getPlayer().getPlayingTrack();
                long position = track != null ? manager.getPlayer().getTrackPosition() : 0;
                if (track == null || !tokenCompare(track.getInfo().title, presence.getTitle(), presence.getAuthor())) {
                    ListenCommand.this.logger.info("Changing track expected {} but got {}", presence.getTitle(), track == null ? "NULL" : track.getInfo().title);
                    manager.getLink().getRestClient().getYoutubeSearchResult(presence.getTitle() + " - " + presence.getAuthor()).thenAccept(tracks -> {
                        if (tracks.isEmpty()) {
                            error.set(true);
                            return;
                        }
                        boolean found = false;
                        for (AudioTrack t : tracks) {
                            if (tokenCompare(t.getInfo().title, presence.getTitle(), presence.getAuthor())) {
                                found = true;
                                AudioPlaylist playlist = new BasicAudioPlaylist("Search Results", tracks, tracks.get(0), true);
                                GuildMusicManager.playlistLoaded(user, playlist, 1);
                                manager.getScheduler().skipTo(t);
                                manager.getPlayer().seekTo(presence.getPosition());
                                break;
                            }
                        }
                        if (!found) {
                            error.set(true);
                        }
                    });
                } else if (Math.abs(presence.getPosition() - position) > 5000) {
                    ListenCommand.this.logger.info("We detected drift, expected {} but got {}", TimeUtil.format(presence.getPosition()), TimeUtil.format(position));
                }
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored) {
                }
            }
            if (error.get()) {
                user.sendMessage("Sorry Senpai~, I lost you :c");
                manager.listeners.remove(user.getUser().getIdLong());
            }
        }
    }
}
