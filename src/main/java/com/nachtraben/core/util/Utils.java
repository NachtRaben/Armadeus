package com.nachtraben.core.util;

import com.nachtraben.core.command.GuildCommandSender;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.*;

public class Utils {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
    private static final ExecutorService EXEC = Executors.newCachedThreadPool();
    private static final Random R = new Random();

    static {
        ((ScheduledThreadPoolExecutor)SCHEDULER).setRemoveOnCancelPolicy(true);
    }

    public static Color randomColor() {
        return new Color(R.nextInt(255), R.nextInt(255), R.nextInt(255));
    }

    public static int getEmbedLength(EmbedBuilder builder) {
        return builder.build().getLength();
    }

    public static ScheduledExecutorService getScheduler() {
        return SCHEDULER;
    }

    public static ExecutorService getExecutor() {
        return EXEC;
    }

    public static void stopExecutors() {
        SCHEDULER.shutdown();
        EXEC.shutdown();
        try {
            EXEC.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            EXEC.shutdownNow();
        }
        try {
            SCHEDULER.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            SCHEDULER.shutdownNow();
        }
    }

    public static EmbedBuilder getAudioTrackEmbed(AudioTrack track, GuildCommandSender sender) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Now Playing: ",
                EmbedBuilder.URL_PATTERN.matcher(track.getInfo().uri).matches()
                        ? track.getInfo().uri : null, null)
                .setColor(Utils.randomColor())
                .setFooter("Requested by: " + sender.getMember().getEffectiveName(), sender.getUser().getAvatarUrl())
                .setDescription(String.format("Title: %s\nAuthor: %s\nLength: %s",
                        track.getInfo().title,
                        track.getInfo().author,
                        track.getInfo().isStream ? "Stream" : TimeUtil.format(track.getInfo().length)));
        if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
        return builder;
    }

}
