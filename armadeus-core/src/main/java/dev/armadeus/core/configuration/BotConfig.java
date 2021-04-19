package dev.armadeus.core.configuration;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.armadeus.core.DiscordBot;
import groovy.lang.Tuple3;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.armadeus.core.util.ConfigUtil.getList;
import static dev.armadeus.core.util.ConfigUtil.setNode;

public class BotConfig {

    private static final Logger logger = LogManager.getLogger();

    private static final CommentedConfigurationNode defaults = CommentedConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(true));

    private static final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
            .path(Path.of("config.conf"))
            .build();

    private static BotConfig instance;

    static {
        try {
            defaults.node("token").act(n -> {
                n.commentIfAbsent("Bot token used to sign into discord");
                n.getString("");
            });
            defaults.node("shards").act(n -> {
                n.commentIfAbsent("Number of discord shards used to handle events");
                n.getInt(-1);
            });
            defaults.node("ownerIds").act(n -> {
                n.commentIfAbsent("List of Owners with all permissions to the bot");
                n.getList(Long.class, Collections.singletonList(118255810613608451L));
            });
            defaults.node("developerIds").act(n -> {
                n.commentIfAbsent("List of Developers with permissions to in-dev commands");
                n.getList(Long.class, Collections.singletonList(118255810613608451L));
            });
            defaults.node("defaultPrefixes").act(n -> {
                n.commentIfAbsent("List of default prefixes to use if the guild did not specify any");
                n.getList(String.class, Collections.singletonList("./"));
            });
            defaults.node("lavalink").act(n -> {
                n.commentIfAbsent("General configuration of lavalink services");
                n.node("nodes").act(n2 -> {
                    n2.commentIfAbsent("Nodes used to handle audio routing");
                    n2.getList(String.class, List.of("localhost;ws://127.0.0.1:2333;fluffy"));
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CommentedConfigurationNode root;

    public static void load() {
        if (instance == null) {
            instance = new BotConfig();
        }
        try {
            instance.root = loader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
            instance.root.mergeFrom(defaults);
        } catch (ConfigurateException e) {
            logger.error("Failed to load bot configuration", e);
        }
    }

    public static void save() {
        if (instance != null) {
            try {
                loader.save(instance.root);
            } catch (ConfigurateException e) {
                logger.error("Failed to save configuration", e);
            }
        }
    }

    public static BotConfig get() {
        if (instance == null)
            throw new IllegalStateException();
        return instance;
    }

    public String getToken() {
        return DiscordBot.getOptions().hasArgument("token") ? DiscordBot.getOptions().valueOf("token").toString() : root.node("token").getString();
    }

    public int getShardCount() {
        int shards = root.node("shards").getInt();
        if (shards < 1) {
            try {
                shards = getRecommendedShardCount();
                setNode(root.node("shards"), Integer.class, shards);
                save();
            } catch (IOException e) {
                logger.warn("Failed to get recommended shard count", e);
            }
        }
        return root.node("shards").getInt();
    }

    private int getRecommendedShardCount() throws IOException {
        Preconditions.checkArgument(getToken() != null && !getToken().isEmpty(), "No bot token configured");
        Request request = new Request.Builder()
                .url("https://discordapp.com/api/gateway/bot")
                .header("Authorization", "Bot " + getToken())
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IllegalStateException("Error requesting recommended shard count: " + response.code() + " " + response.message());
            }
            JsonObject data = JsonParser.parseString(body.string()).getAsJsonObject();
            return data.get("shards").getAsInt();
        }
    }

    public List<String> getGlobalPrefixes() {
        return getList(root.node("defaultPrefixes"), String.class);
    }

    public List<Long> getOwnerIds() {

        try {
            return root.node("ownerIds").getList(Long.class, Collections.singletonList(118255810613608451L));
        } catch (SerializationException e) {
            logger.error(e);
            return Collections.singletonList(118255810613608451L);
        }
    }

    public List<Long> getDeveloperIds() {
        try {
            List<Long> developers = new ArrayList<>();
            developers.addAll(root.node("developerIds").getList(Long.class, Collections.singletonList(118255810613608451L)));
            developers.addAll(getOwnerIds());
            return developers;
        } catch (SerializationException e) {
            logger.error(e);
            return Collections.singletonList(118255810613608451L);
        }
    }

    public List<Tuple3<String, String, String>> getLavalinkNodes() {
        ConfigurationNode node = root.node("lavalink", "nodes");
        try {
            List<String> links = node.getList(String.class);
            List<Tuple3<String, String, String>> nodes = new ArrayList<>();
            for (String link : links) {
                String[] tokens = link.split(";");
                nodes.add(new Tuple3<>(tokens[0], tokens[1], tokens[2]));
            }
            return nodes;
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return List.of();
    }
}