package dev.armadeus.bot.commands.audio;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FuckingTestCommand {

    private static ExecutorService EXEC = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());


    private boolean isCorrectTrack(String title, String author, String trackTitle) {
        List<String> expected = Arrays.stream((title + " " + author).toLowerCase().split(" ")).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
        List<String> tokens = Arrays.stream(trackTitle.toLowerCase().split(" ")).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
        int matches = 0;
        for (String token : tokens) {
            for (String exp : expected) {
                if (token.equals(exp))
                    matches++;
            }
        }
        System.out.println(expected + " <> " + tokens + " <> " + matches + " <> " + (0.4f * expected.size()));
        return (float) matches >= (0.4f * expected.size());
    }

    @Cmd(name = "listen", format = "", description = "Listens to you over spotify")
    public void listen(CommandSender cs) {
        if (!(cs instanceof GuildCommandSender)) return;
        GuildCommandSender sender = (GuildCommandSender) cs;
        if (sender.getUser().getIdLong() != 118255810613608451L && sender.getUser().getIdLong() != 135923847147945985L) return;
        GuildMusicManager manager = sender.getGuildConfig().getMusicManager();
        if (manager.listeners.containsKey(sender.getUser().getIdLong())) return;

        if (sender.getMember().getVoiceState().inVoiceChannel()) {
            manager.listeners.put(sender.getUser().getIdLong(), EXEC.submit(() -> {
                sender.sendMessage("Now listening to you senpai!~");
                AtomicBoolean fuck = new AtomicBoolean(false);
                try {
                    while (!Thread.currentThread().isInterrupted() && sender.getMember().getVoiceState().inVoiceChannel() && !fuck.get()) {
                        Member member = sender.getMember();
                        Activity act = member.getActivities().stream().filter(a -> a.getName().equalsIgnoreCase("spotify")).findFirst().orElse(null);
                        if (act != null) {
                            RichPresence presence = act.asRichPresence();
                            String title = presence.getDetails();
                            String author = presence.getState();
                            long duration = presence.getTimestamps().getElapsedTime(ChronoUnit.MILLIS);
                            AudioTrack track = manager.getPlayer().getPlayingTrack();
                            long position = track != null ? manager.getPlayer().getTrackPosition() : 0;
                            if (track == null || !isCorrectTrack(title, author, track.getInfo().title)) {
                                System.out.printf("Changing track expected %s but got %s%n", title, track == null ? "NULL" : track.getInfo().title);
                                System.out.println(manager.getLink().getState());
                                manager.getLink().getRestClient().getYoutubeSearchResult(title + " - " + author).thenAccept(tracks -> {
                                    if (tracks.isEmpty()) {
                                        System.out.println("No tracks? Nani dafuq");
                                        return;
                                    }
                                    for (AudioTrack t : tracks) {
                                        if (!isCorrectTrack(title, author, t.getInfo().title)) {
                                            fuck.set(true);
                                            return;
                                        }
                                        AudioPlaylist playlist = new BasicAudioPlaylist("Search Results", tracks, tracks.get(0), true);
                                        manager.getScheduler().stop();
                                        AudioPlayCommand.playlistLoaded(playlist, sender, false, 1);
                                        manager.getPlayer().seekTo(duration);
                                        break;
                                    }
                                });
                            } else if (duration - position > 5000) {
                                System.out.printf("Duration: %s <> Position: %s <> Delta: %s%n", duration, position, duration - position);
                                manager.getPlayer().seekTo(duration);
                            }
                        }
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sender.sendMessage("Sorry senpai, i lost you :c");
                manager.listeners.remove(sender.getUser().getIdLong());
                System.out.println("Listener terminated!");
            }));
        }

    }

}
