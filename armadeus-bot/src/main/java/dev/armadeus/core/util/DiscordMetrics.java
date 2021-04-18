package dev.armadeus.core.util;

import dev.armadeus.core.DiscordBot;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordMetrics implements Runnable {

    private static final Logger logger = LogManager.getLogger();
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();

    private final DiscordBot dbot;
    private final File output;

    public DiscordMetrics(DiscordBot dbot) {
        this.dbot = dbot;
        long start = System.currentTimeMillis();
        try {
            dbot.getShardManager().getShards().get(0).awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Took " + (System.currentTimeMillis() - start) + "ms for the self user to initialize!");
        output = new File(dbot.getShardManager().getShards().get(0).getSelfUser().getId() + ".metrics");
        if (!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EXEC.scheduleAtFixedRate(this, 5L, 60L, TimeUnit.MINUTES);
        logger.debug("Initialized new DiscordMetrics for: " + output.getName());
    }

    @Override
    public void run() {
        logger.info("Writing DiscordMetrics to: " + output.getName());
        try (FileWriter fw = new FileWriter(output, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            ZonedDateTime zdt = ZonedDateTime.now();
            ShardManager manager = dbot.getShardManager();
            String toWrite = String.format("date:%s\tguilds:%s\tusers:%s\ttchannels:%s\tvchannels:%s\n",
                    zdt.format(DateTimeFormatter.ofPattern("MM-dd-yyyy_HH:mm:ss")),
                    manager.getGuilds().size(), manager.getUsers().size(), manager.getTextChannels().size(), manager.getVoiceChannels().size());
            if (!dbot.isDevMode())
                out.write(toWrite);
            else
                logger.debug("[Metrics] >> " + toWrite);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void shutdown() {
        EXEC.shutdown();
    }
}
