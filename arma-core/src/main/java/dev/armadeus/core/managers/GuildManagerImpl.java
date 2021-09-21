package dev.armadeus.core.managers;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.json.JsonFormat;
import com.electronwill.nightconfig.json.MinimalJsonWriter;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.plugin.util.DummyPluginContainer;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.guild.GuildManager;
import dev.armadeus.bot.database.Tables;
import dev.armadeus.bot.database.tables.records.GuildsRecord;
import dev.armadeus.core.config.ArmaConfigImpl;
import dev.armadeus.core.config.GuildConfigImpl;
import dev.armadeus.core.config.NestedConfig;
import dev.armadeus.core.util.ConfigUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class GuildManagerImpl implements GuildManager {

    private static final Logger logger = LoggerFactory.getLogger(GuildManager.class);

    private ArmaCore core;
    private Path guildDir;
    private Cache<Long, GuildConfig> cache;
    private Connection conn;
    private ScheduledTask saveFuture;

    private final Function<Long, Config> loadDatabaseConfig = guildId -> {
        logger.info("Loading guild configuration {}", guildId);
        DSLContext context = DSL.using(conn);
        GuildsRecord result = context.selectFrom(Tables.GUILDS).where(Tables.GUILDS.ID.eq(guildId)).fetchOne();
        if(result != null) {
            JsonFormat<MinimalJsonWriter> format = JsonFormat.minimalInstance();
            return format.createParser().parse(result.getConfig());
        }
        return JsonFormat.minimalInstance().createConfig();
    };

    private final Function<Long, Config> loadFileConfig = guildId -> {
        Path path = guildDir.resolve(guildId + ".toml");
        FileConfig config = FileConfig.builder(Files.exists(path) ? path : guildDir.resolve(guildId + ".json"))
                .preserveInsertionOrder()
                .sync()
                .concurrent()
                .build();
        config.load();
        return config;
    };

    public GuildManager initialize(ArmaCore core, Path guildDir) {
        this.core = core;
        this.guildDir = guildDir;
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
        if (core.armaConfig().isDatabaseEnabled()) {
            initializeDatabase();
        }
        return this;
    }

    private void initializeDatabase() {
        try {
            ArmaConfigImpl.Database db = ((ArmaConfigImpl) core.armaConfig()).getDatabaseInfo();
            conn = DriverManager.getConnection(db.getUri(), db.getUsername(), db.getPassword());
            logger.warn("Enabled SQL based guild configurations");
            saveFuture = core.scheduler().buildTask(DummyPluginContainer.VELOCITY, this::saveGuilds).repeat(5, TimeUnit.SECONDS).schedule();
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            System.exit(-1);
        }
    }

    private void saveGuilds() {
        MinimalJsonWriter jsonWriter = JsonFormat.minimalInstance().createWriter();
        for (Map.Entry<Long, GuildConfig> entry : cache.asMap().entrySet()) {
            long guildId = entry.getKey();
            NestedConfig config = (NestedConfig) ((GuildConfigImpl) entry.getValue()).getConfig();
            AtomicBoolean needsSaved = config.needsSaved();
            if (needsSaved.get()) {
                logger.info("Saving guild configuration for {}", guildId);
                try (StringWriter writer = new StringWriter()) {
                    jsonWriter.write(config, writer);
                    DSLContext c2 = DSL.using(conn, SQLDialect.POSTGRES);
                    c2.insertInto(Tables.GUILDS)
                            .set(Tables.GUILDS.ID, guildId)
                            .set(Tables.GUILDS.CONFIG, writer.toString())
                            .onDuplicateKeyUpdate()
                            .set(Tables.GUILDS.CONFIG, writer.toString())
                            .execute();
                    needsSaved.set(false);
                } catch (IOException e) {
                    logger.error("Failed to save guild configuration for " + guildId, e);
                }
            }
        }
    }

    @Override
    public GuildConfig getConfigFor(Guild guild) {
        return getConfigFor(guild.getIdLong());
    }

    @Override
    public GuildConfig getConfigFor(long guildId) {
        try {
            return cache.get(guildId, () -> {
                URL defaultGuildConfigLocation = GuildManagerImpl.class.getClassLoader().getResource("configs/guild-config.toml");
                assert defaultGuildConfigLocation != null;
                checkNotNull(defaultGuildConfigLocation, "Default guild configuration does not exist");
                Config defaults = TomlFormat.instance().createParser().parse(defaultGuildConfigLocation);
                Config config;
                if (core.armaConfig().isDatabaseEnabled()) {
                    config = new NestedConfig(guildId, loadDatabaseConfig.apply(guildId));
                } else  {
                    config = new NestedConfig(guildId, loadFileConfig.apply(guildId));
                }
                // Merges defaults into the config, saving if necessary
                ConfigUtil.merge(defaults, config);
                return new GuildConfigImpl(guildId, config);
            });
        } catch (ExecutionException e) {
            logger.error("Failed to load guild configuration for " + guildId, e);
            return null;
        }
    }

    @Override
    public void shutdown() {
        logger.warn("Shutting down GuildManager");
        if (saveFuture != null) {
            saveFuture.cancel();
            saveGuilds();
        }
    }
}
