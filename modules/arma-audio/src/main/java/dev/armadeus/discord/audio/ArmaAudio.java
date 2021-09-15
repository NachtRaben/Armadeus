package dev.armadeus.discord.audio;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.MessageFormatter;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.inject.Inject;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.events.ShutdownEvent;
import dev.armadeus.discord.audio.database.Tables;
import dev.armadeus.discord.audio.database.tables.records.LavalinkRecord;
import lavalink.client.io.LavalinkLoadBalancer;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@Plugin(id = "arma-audio", name = "Arma-Audio", version = "0.1", url = "https://armadeus.com", description = "Default audio implementation", authors = { "NachtRaben" })
public class ArmaAudio {

    private static final Logger logger = LoggerFactory.getLogger(ArmaAudio.class);
    @Getter
    @Accessors(fluent = true)
    private static ArmaAudio get;

    private final Map<Long, AudioManager> managers = new HashMap<>();
    private final JdaLavalink lavalink;

    @Getter
    @Accessors(fluent = true)
    private static ArmaCore core;

    @Inject
    public ArmaAudio(Velocity core) {
        logger.info("Initializing Arma-Audio...");
        ArmaAudio.core = (ArmaCore) core;
        get = this;
        lavalink = new JdaLavalink(core().armaConfig().getShardsTotal(), shardId -> core().shardManager().getShardById(shardId));
        lavalink.getLoadBalancer().addPenalty(LavalinkLoadBalancer.Penalties::getTotal);
    }

    @Subscribe
    public void initializeBuilder(DefaultShardManagerBuilder builder) {
        logger.info("Registering voice interceptors...");
        builder.addEventListeners(lavalink);
        builder.setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor());
    }

    @Subscribe
    public void connectedToDiscord(ShardManager manager) {
        lavalink.setUserId(manager.getShards().get(0).getSelfUser().getId());
        logger.info("Registering LavaLink nodes...");
        if (core.armaConfig().isDatabaseEnabled()) {
            Connection conn = core().armaConfig().createDatabaseConnection();
            logger.warn("Searching database for lavalink nodes...");
            DSLContext context = DSL.using(conn);
            List<LavalinkRecord> records = context.select(DSL.asterisk()).from(Tables.LAVALINK).fetchInto(LavalinkRecord.class);
            for (LavalinkRecord record : records) {
                registerNode(record.getId(), record.getUri(), record.getPassword());
            }
        } else {
            CommentedConfig config = core().armaConfig().getMetadataOrInitialize("lavalink", c -> c.add("localhost", "ws://localhost:2333;fluffy"));
            for (CommentedConfig.Entry s : config.entrySet()) {
                String[] tokens = s.<String>getValue().split(";");
                if (tokens.length != 2)
                    continue;
                registerNode(s.getKey(), tokens[0], tokens[1]);
            }
        }
    }

    @Subscribe
    public void onShutdown(ShutdownEvent event) {
        lavalink.getLinks().forEach(Link::destroy);
        lavalink.shutdown();
    }

    private void registerNode(String name, String uri, String password) {
        try {
            lavalink.addNode(name, new URI(uri), password);
            logger.info("Registered LavaLink node {}@{}", name, uri);
        } catch (URISyntaxException e) {
            logger.error("Failed to register LavaLink node " + uri, e);
        }
    }


    @SuppressWarnings("rawtypes")
    @Subscribe
    public void registerCommands(CommandManager manager) {
        logger.info("Registering Arma-Audio commands...");
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(ArmaAudio.class.getClassLoader())
                .setScanners(new SubTypesScanner())
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.discord", ArmaAudio.class.getClassLoader()))
                .filterInputsBy(new FilterBuilder().includePackage("dev.armadeus.discord"))
        );
        reflections.getSubTypesOf(DiscordCommand.class).forEach(clazz -> {
            try {
                if (!clazz.isSynthetic() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    manager.registerCommand(clazz.getConstructor().newInstance());
                    logger.info("Registered command {}", clazz.getSimpleName());
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                logger.error("Failed to register command class " + clazz.getSimpleName(), e);
            }
        });
    }

    public static AudioManager getManagerFor(Guild guild) {
        return get.managers.computeIfAbsent(guild.getIdLong(), l -> new AudioManager(guild));
    }

    public static AudioManager getManagerFor(long guildId) {
        return getManagerFor(Objects.requireNonNull(ArmaAudio.core().shardManager().getGuildById(guildId)));
    }
}
