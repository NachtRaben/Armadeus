package dev.armadeus.core.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setDaemon(true).build());
    public static final ExecutorService EXEC = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
    private static final Random R = new Random();

    static {
        ((ScheduledThreadPoolExecutor) SCHEDULER).setRemoveOnCancelPolicy(true);
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

}