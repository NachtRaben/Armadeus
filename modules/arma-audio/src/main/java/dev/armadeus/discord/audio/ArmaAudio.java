package dev.armadeus.discord.audio;

import co.aikar.commands.CommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.core.config.ArmaConfigImpl;
import dev.armadeus.discord.audio.database.Tables;
import dev.armadeus.discord.audio.database.tables.records.LavalinkRecord;
import lavalink.client.io.LavalinkLoadBalancer;
import lavalink.client.io.jda.JdaLavalink;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@Plugin(id = "arma-audio", name = "Arma-Audio", version = "0.1", url = "https://armadeus.com", description = "Default audio implementation", authors = { "NachtRaben" })
public class ArmaAudio {

    private static final Logger logger = LoggerFactory.getLogger(ArmaAudio.class);
    private static ArmaAudio instance;

    private final Map<Long, AudioManager> managers = new HashMap<>();
    private final JdaLavalink lavalink;
    private final ArmaCore core;

    @Inject
    public ArmaAudio(ArmaCore core) {
        logger.info("Initializing Arma-Audio...");
        this.core = core;
        instance = this;
        lavalink = new JdaLavalink(1, shardId -> core.getShardManager().getShardById(shardId));
        lavalink.getLoadBalancer().addPenalty(LavalinkLoadBalancer.Penalties::getTotal);
    }

    public static ArmaAudio get() {
        return instance;
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
        if(core.getArmaConfig().isDatabaseEnabled()) {
            logger.warn("Enabled SQL based node storage");
            try {
                ArmaConfigImpl.Database db = ((ArmaConfigImpl) core.getArmaConfig()).getDatabaseInfo();
                Connection conn = DriverManager.getConnection(db.getUri(), db.getUsername(), db.getPassword());
                DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
                List<LavalinkRecord> records = context.select(DSL.asterisk()).from(Tables.LAVALINK).fetchInto(LavalinkRecord.class);
                for(LavalinkRecord record : records) {
                    try {
                        lavalink.addNode(record.getId(), new URI(record.getUri()), record.getPassword());
                        logger.info("Added LavaLink node {}@{}", record.getId(), record.getUri());
                    } catch (URISyntaxException e) {
                        logger.error("Failed to register LavaLink node " + record.getUri(), e);
                    }
                }
                logger.warn("Enabled SQL based guild configurations");
            } catch (SQLException e) {
                logger.error("Failed to connect to database", e);
            }
        } else {
            try {
                lavalink.addNode("localhost", new URI("wss://localhost:2333"), "fluffy");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(getClass().getClassLoader())
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.discord", getClass().getClassLoader()))
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
        return instance.managers.computeIfAbsent(guild.getIdLong(), l -> new AudioManager(guild));
    }

    public static AudioManager getManagerFor(long guildId) {
        return getManagerFor(Objects.requireNonNull(ArmaCore.get().getShardManager().getGuildById(guildId)));
    }

}
