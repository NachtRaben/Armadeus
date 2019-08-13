package com.nachtraben.core.util;

import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebhookLogger implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WebhookLogger.class);

    private final Object lock = new Object();
    private List<String> messages;

    private DiscordBot dbot;
    private long guildId;
    private long channelId;

    private WebhookClient webhook;
    private ScheduledFuture future;

    public WebhookLogger(DiscordBot bot, long guildId, long channelId) {
        log.info("Initialized logger for " + guildId);
        this.dbot = bot;
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public void start() {
        Guild g = dbot.getShardManager().getGuildByID(guildId);
        Webhook webhook = g.getWebhooks().complete().stream().filter(wh -> wh.getChannel().getIdLong() == channelId).findFirst().orElse(null);
        if(webhook == null)
            throw new IllegalArgumentException("Cannot find webhook in " + g.getName() + " with id " + channelId);
        this.webhook = new WebhookClientBuilder(webhook).build();
        messages = new ArrayList<>();
        future = Utils.getScheduler().scheduleWithFixedDelay(this, 0L, 2L, TimeUnit.SECONDS);
        log.info("Logger started for " + webhook.getName());
    }

    @Override
    public void run() {
        synchronized (lock) {
            List<String> toSend = new ArrayList<>();
            if(!messages.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("```text\n");
                Iterator<String> it = messages.iterator();
                while(it.hasNext()) {
                    String s = it.next();
                    String line = s.substring(0, Math.min(2000 - 13, s.length())).replaceAll("\u001B\\[[;\\d]*m", "").replace("`", "") + "\n";
                    if (builder.length() + line.length() > 2000 - 12) {
                        builder.append("```");
                        toSend.add(builder.toString());
                        builder.setLength(0);
                        builder.append("```text\n");
                    }
                    builder.append(line);
                    it.remove();
                }
                builder.append("```");
                toSend.add(builder.toString());
                for (String message : toSend)
                    webhook.send(message);
            }
        }
    }

    public void send(String s) {
        synchronized (lock) {
            messages.add(s);
        }
    }

    public void stop() {
        log.info("Shutting down webhook logger.");
        if(webhook != null)
            webhook.close();
        if(future != null && !future.isCancelled() && !future.isDone())
            future.cancel(false);
    }
}
