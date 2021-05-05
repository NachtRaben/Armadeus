package dev.armadeus.core.guild;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.guild.GuildManager;
import dev.armadeus.bot.database.Tables;
import dev.armadeus.bot.database.tables.records.GuildsRecord;
import dev.armadeus.core.config.ArmaConfigImpl;
import dev.armadeus.core.config.SaveNotifyConfig;
import dev.armadeus.core.config.GuildConfigImpl;
import dev.armadeus.core.plugin.ArmaCorePlugin;
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

    private dev.armadeus.bot.api.ArmaCore core;
    private Path guildDir;
    private Cache<Long, GuildConfig> cache;
    private Connection conn;
    private ScheduledTask saveFuture;

    private Function<Long, CommentedConfig> loadDatabaseConfig = guildId -> {
        TomlFormat format = TomlFormat.instance();
        TomlParser parser = format.createParser();
        DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
        GuildsRecord result = context.selectFrom(Tables.GUILDS).where(Tables.GUILDS.ID.eq(guildId)).fetchOne();
        return result != null ? parser.parse(result.getConfig()) : format.createConfig();
    };

    private Function<Long, CommentedConfig> loadFileConfig = guildId -> {
        // Creates a toml file config with default values
        Path path = guildDir.resolve(guildId + ".toml");
        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .preserveInsertionOrder()
                .autosave()
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
        if (core.getArmaConfig().isDatabaseEnabled()) {
            initializeDatabase();
        }
        return this;
    }

    private void initializeDatabase() {
        try {
            ArmaConfigImpl.Database db = ((ArmaConfigImpl) core.getArmaConfig()).getDatabaseInfo();
            conn = DriverManager.getConnection(db.getUri(), db.getUsername(), db.getPassword());
            saveFuture = core.getScheduler().buildTask(ArmaCorePlugin.get(), this::saveGuilds).repeat(5, TimeUnit.SECONDS).schedule();
            logger.warn("Enabled SQL based guild configurations");
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
        }
    }

    private void saveGuilds() {
        TomlWriter tomlWriter = TomlFormat.instance().createWriter();
        for (Map.Entry<Long, GuildConfig> entry : cache.asMap().entrySet()) {
            long guildId = entry.getKey();
            SaveNotifyConfig config = (SaveNotifyConfig) ((GuildConfigImpl) entry.getValue()).getConfig();
            AtomicBoolean needsSaved = config.needsSaved();
            if (needsSaved.get()) {
                logger.info("Saving guild configuration for {}", guildId);
                try (StringWriter writer = new StringWriter()) {
                    tomlWriter.write(config, writer);
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
                CommentedConfig defaults = TomlFormat.instance().createParser().parse(defaultGuildConfigLocation);
                CommentedConfig config;
                if (core.getArmaConfig().isDatabaseEnabled()) {
                    config = new SaveNotifyConfig(guildId, loadDatabaseConfig.apply(guildId));
                } else  {
                    config = loadFileConfig.apply(guildId);
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
