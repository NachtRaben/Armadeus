package com.nachtraben.core.util;

import com.nachtraben.core.command.GuildCommandSender;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    public static byte[] serialize(Object o) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(baos)) {
            out.writeObject(o);
            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bais)) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
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
                        track.getInfo().isStream ? "Stream" : TimeUtil.fromLong(track.getInfo().length, TimeUtil.FormatType.STRING)));
        if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail(String.format("https://img.youtube.com/vi/%s/default.jpg", track.getIdentifier()));
        return builder;
    }

}
