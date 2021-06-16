package dev.armadeus.discord.audio;

import co.aikar.commands.CommandManager;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.inject.Inject;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.discord.audio.database.Tables;
import dev.armadeus.discord.audio.database.tables.records.LavalinkRecord;
import lavalink.client.io.LavalinkLoadBalancer;
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
            logger.warn("Enabled SQL based lavalink nodes");
            DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
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

    private void registerNode(String name, String uri, String password) {
        try {
            lavalink.addNode(name, new URI(uri), password);
            logger.info("Added LavaLink node {}@{}", name, uri);
        } catch (URISyntaxException e) {
            logger.error("Failed to register LavaLink node " + uri, e);
        }
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(ArmaAudio.class.getClassLoader())
                .setScanners(new SubTypesScanner())
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.discord", ArmaAudio.class.getClassLoader()))
                .filterInputsBy(new FilterBuilder().includePackage("dev.armadeus.discord"))
        );
        Set<Class<? extends DiscordCommand>> commandClazzes = reflections.getSubTypesOf(DiscordCommand.class);
        commandClazzes.forEach(clazz -> {
            try {
                if (!clazz.isSynthetic() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    logger.info("Registering command class {}", clazz.getSimpleName());
                    manager.registerCommand(clazz.getConstructor().newInstance());
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                logger.error("Failed to register command class", e);
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
