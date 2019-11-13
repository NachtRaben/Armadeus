package com.nachtraben.core.managers;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.listeners.DiscordCommandListener;
import com.nachtraben.core.listeners.FileUploadListener;
import com.nachtraben.core.listeners.WelcomeListener;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ShardManager {

    private static final Logger log = LoggerFactory.getLogger(ShardManager.class);

    private SessionController queue;
    private DiscordBot bot;
    private List<JDA> shards;
    private Set<ListenerAdapter> defaultListeners;
    private int shardCount;
    private long nextRestart = System.currentTimeMillis();

    public ShardManager(DiscordBot bot) {
        this.bot = bot;
        shards = new ArrayList<>();
        queue = new SessionControllerAdapter();
        defaultListeners = new HashSet<>();
        shardCount = bot.getConfig().getShardCount();
        if(shardCount == -1) {
            log.warn("Getting recommended shard count from discord...");
            shardCount = getRecommendedShardCount();
        }
        if(shardCount < 1 || shardCount == 2)
            throw new IllegalArgumentException("Shard count must be at least 1 or greater than 3!");

        log.info("Initializing with { " + shardCount + " } shards.");
    }

    public void connectShard(int shard) {
        if(shard < 0 || shard >= shardCount)
            throw new IllegalArgumentException("Invalid shard count provided!");

        long wait = nextRestart - System.currentTimeMillis();
        if(wait > 0) {
            try {
                log.warn("Waiting { " + wait + " }ms before starting next shard.");
                Thread.sleep(wait);
                nextRestart = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(shard < shards.size()) {
            JDA jda = shards.get(shard);
            if(jda != null && !jda.getStatus().equals(JDA.Status.DISCONNECTED)) {
                log.warn("Attempted to shut down a running shard, restarting it for you.");
                jda.shutdown();
            }
        }

        log.info("Starting shard " + shard + " [" + (shard + 1) + "/" + (shardCount) + "].");
        JDABuilder builder = initBuilder();
        if(shardCount == 1) {
            builder.setActivity(Activity.of(Activity.ActivityType.DEFAULT, "shard [1/1]"));
        } else {
            builder.setActivity(Activity.of(Activity.ActivityType.DEFAULT, "shard [" + shard + "/" + (shardCount - 1) + "]"));
            builder.useSharding(shard, shardCount);
        }
        try {
            shards.add(shard, builder.build());
        } catch (LoginException e) {
            log.error("Failed to login to discord.", e);
        }
    }

    public void connectAllShards() {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(bot.getConfig().getBotToken());
        for(int i = 0; i < shardCount; i++) {
            connectShard(i);
        }
    }

    public void shutdownShard(int shard) {
        if(shard < 0 || shard >= shards.size())
            throw new IllegalArgumentException("Invalid shard count provided!");
        shards.get(shard).shutdown();
    }

    public void shutdownAllShards() {
        shards.forEach(JDA::shutdown);
    }

    public void addDefaultListener(ListenerAdapter... listeners) {
        Collections.addAll(defaultListeners, listeners);
    }

    private JDABuilder initBuilder() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(bot.getConfig().getBotToken());
        builder.setEventManager(new ExecutorServiceEventManager());
        builder.setSessionController(queue);
        builder.setAudioSendFactory(new NativeAudioSendFactory());
        defaultListeners.forEach(builder::addEventListeners);
        return builder;
    }


    private int getRecommendedShardCount() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + bot.getConfig().getBotToken())
                    .header("Content-Type", "application/json")
                    .get().build();

            Response resp = client.newCall(req).execute();
            InputStream in = resp.body().byteStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            JSONObject json = new JSONObject(new JSONTokener(reader));
            reader.close();
            return json.getInt("shards");
        } catch(Exception e) {
            log.warn("Failed to get recommended shard count, assuming 1.", e);
            return 1;
        }
    }

    public TextChannel getTextChannelByID(long textChannelId) {
        TextChannel channel;
        for(JDA jda : shards) {
            channel = jda.getTextChannelById(textChannelId);
            if(channel != null)
                return channel;
        }
        return null;
    }

    public User getUserByID(long userID) {
        User user;
        for(JDA jda : shards) {
            user = jda.getUserById(userID);
            if(user != null)
                return user;
        }
        return null;
    }

    public PrivateChannel getPrivateChannelByID(long messageChannelID) {
        PrivateChannel channel;
        for(JDA jda : shards) {
            channel = jda.getPrivateChannelById(messageChannelID);
            if(channel != null)
                return channel;
        }
        return null;
    }

    public List<JDA> getShards() {
        return shards;
    }

    public Set<ListenerAdapter> getDefaultListeners() {
        return new HashSet<>(defaultListeners);
    }

    public int getShardCount() {
        return shardCount;
    }

    public Guild getGuildByID(Long guildID) {
        Guild guild;
        for(JDA jda : shards) {
            guild = jda.getGuildById(guildID);
            if(guild != null)
                return guild;
        }
        return null;
    }
}
