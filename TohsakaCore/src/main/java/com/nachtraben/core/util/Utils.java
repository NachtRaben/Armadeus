package com.nachtraben.core.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
    }

}
