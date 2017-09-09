package com.nachtraben.core.util;

import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordMetrics implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordMetrics.class);
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();

    private DiscordBot dbot;
    private File output;

    public DiscordMetrics(DiscordBot dbot) {
        this.dbot = dbot;
        long start = System.currentTimeMillis();
        while (dbot.isRunning() && dbot.getShardManager().getShards().get(0).getSelfUser() == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("Took " + (System.currentTimeMillis() - start) + "ms for the self user to initialize!");
        output = new File(dbot.getShardManager().getShards().get(0).getSelfUser().getId() + ".metrics");
        if (!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EXEC.scheduleAtFixedRate(this, 5L, 60L, TimeUnit.MINUTES);
        LOGGER.debug("Initialized new DiscordMetrics for: " + output.getName());
    }

    @Override
    public void run() {
        LOGGER.info("Writing DiscordMetrics to: " + output.getName());
        try (FileWriter fw = new FileWriter(output, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            ZonedDateTime zdt = ZonedDateTime.now();
            String toWrite = String.format("date:%s\tguilds:%s\tusers:%s\ttchannels:%s\tvchannels:%s\n",
                    zdt.format(DateTimeFormatter.ofPattern("MM-dd-yyyy_HH:mm:ss")),
                    dbot.getGuildCount(), dbot.getUserCount(), dbot.getTextChannels(), dbot.getVoiceChannels());
            if(!dbot.isDebugging())
                out.write(toWrite);
            else
                LOGGER.debug("[Metrics] >> " + toWrite);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void shutdown() {
        EXEC.shutdown();
    }
}
