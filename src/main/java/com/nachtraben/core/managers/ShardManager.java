package com.nachtraben.core.managers;

import com.mashape.unirest.http.Unirest;
import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.*;

public class ShardManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardManager.class);

    private DiscordBot bot;
    private List<JDA> shards;
    private Set<EventListener> defaultListeners;
    private int shardCount;
    private JDABuilder builder;


    public ShardManager(DiscordBot bot) {
        this.bot = bot;
        shards = new ArrayList<>();
        defaultListeners = new HashSet<>();
        shardCount = bot.getConfig().getShardCount();
        if(shardCount == -1) {
            LOGGER.warn("Getting recommended shard count from discord...");
            shardCount = getRecommendedShardCount();
        }
        if(shardCount < 1)
            throw new IllegalArgumentException("Shard count must be > 1!");
        LOGGER.info("Initializing with { " + shardCount + "} shards.");
        initBuilder();
    }

    public void connectShard(int shard) {
        if(shard < 0 || shard >= shards.size())
            throw new IllegalArgumentException("Invalid shard count provided!");
        JDA jda = shards.get(shard);
        if(!jda.getStatus().equals(JDA.Status.DISCONNECTED)) {
            LOGGER.warn("Attempted to shut down a running shard, restarting it for you.");
            jda.shutdown(false);
        }

        if(shardCount == 1) {
            builder.setGame(Game.of("1/1"));
        } else {
            builder.setGame(Game.of(shard + "/" + (shardCount - 1)));
            builder.useSharding(shard, shardCount);
        }
        try {
            shards.add(shard, builder.buildAsync());
        } catch (LoginException e) {
            LOGGER.error("Failed to login to discord.", e);
        } catch (RateLimitedException e) {
            try {
                LOGGER.warn(String.format("Failed to login shard { %s } due to rate-limiting, waiting { %s } ms before retrying.", shard, e.getRetryAfter()));
                Thread.sleep(e.getRetryAfter());
                shards.add(shard, builder.buildAsync());
            } catch (InterruptedException | LoginException | RateLimitedException e1) {
                LOGGER.error("Failed to login to discord.", e1);
            }
        }
    }

    public void connectAllShards() {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(bot.getConfig().getBotToken());
        for(int i = 0; i < shardCount; i++) {
            connectShard(i);
        }
    }

    public void shutdownShard(int shard, boolean free) {
        if(shard < 0 || shard >= shards.size())
            throw new IllegalArgumentException("Invalid shard count provided!");
        shards.get(shard).shutdown(free);
    }

    public void shutdownAllShards(boolean free) {
        shards.forEach((jda) -> jda.shutdown(free));
    }

    public void addDefaultListener(EventListener... listeners) {
        Collections.addAll(defaultListeners, listeners);
        initBuilder();
    }

    private void initBuilder() {
        builder = new JDABuilder(AccountType.BOT).setToken(bot.getConfig().getBotToken());
        defaultListeners.forEach(builder::addEventListener);
    }


    private int getRecommendedShardCount() {
        try {
            return Unirest.get("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + bot.getConfig().getBotToken())
                    .header("Content-Type", "application/json")
                    .asJson().getBody().getObject().getInt("shards");
        } catch(Exception e) {
            LOGGER.warn("Failed to get recommended shard count, assuming 1.", e);
            return 1;
        }
    }

    public TextChannel getTextChannelById(long textChannelId) {
        TextChannel channel;
        for(JDA jda : shards) {
            channel = jda.getTextChannelById(textChannelId);
            if(channel != null)
                return channel;
        }
        return null;
    }

    public User getUserById(long userID) {
        User user;
        for(JDA jda : shards) {
            user = jda.getUserById(userID);
            if(user != null)
                return user;
        }
        return null;
    }

    public PrivateChannel getPrivateChannelById(long messageChannelID) {
        PrivateChannel channel;
        for(JDA jda : shards) {
            channel = jda.getPrivateChannelById(messageChannelID);
            if(channel != null)
                return channel;
        }
        return null;
    }
}
