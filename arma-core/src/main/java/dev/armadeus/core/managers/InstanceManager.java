package dev.armadeus.core.managers;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.plugin.util.DummyPluginContainer;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.database.core.tables.records.InstancesRecord;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.armadeus.bot.database.core.tables.Instances.INSTANCES;

public class InstanceManager {

    private static final Logger logger = LogManager.getLogger();

    private ArmaCore core;
    private Connection dbConnection;
    private ScheduledTask task;
    @Getter
    private boolean devActive;

    public InstanceManager(ArmaCore core) {
        logger.info("Starting database managed instance manager");
        this.core = core;
        this.dbConnection = core.armaConfig().createDatabaseConnection();
        this.task = core.scheduler().buildTask(DummyPluginContainer.VELOCITY, this::run).repeat(5, TimeUnit.SECONDS).schedule();
    }

    private void run() {
        logger.info("Tick");
        DSLContext context = DSL.using(dbConnection, SQLDialect.POSTGRES);
        InstancesRecord record = new InstancesRecord(core.armaConfig().getUuid(), core.armaConfig().isDevMode(), System.currentTimeMillis());
        context.insertInto(INSTANCES)
                .set(record)
                .onDuplicateKeyUpdate()
                .set(record).execute();
        List<InstancesRecord> result = context.selectFrom(INSTANCES)
                .where(INSTANCES.DEV_MODE.eq(true))
                .fetch();
        logger.info("Before:\n{}", result);
        result = result.stream().filter(r -> System.currentTimeMillis() - r.getUpdated() < 3000).collect(Collectors.toList());
        logger.info("After:\n{}", result);
        if(devActive && result.isEmpty()) {
            devActive = false;
            if(!core.armaConfig().isDevMode())
                logger.warn("There is no longer a dev instance active, returning to regular functionality");
        } else if(!devActive && !result.isEmpty()) {
            devActive = true;
            if(!core.armaConfig().isDevMode())
                logger.warn("A dev instance is now active, dev guilds will no longer be handled");
        }
    }

    public void shutdown() {
        task.cancel();
        if(core.armaConfig().isDevMode()) {
            DSLContext context = DSL.using(dbConnection, SQLDialect.POSTGRES);
            InstancesRecord record = new InstancesRecord(core.armaConfig().getUuid(), false, System.currentTimeMillis());
            context.insertInto(INSTANCES)
                    .set(record)
                    .onDuplicateKeyUpdate()
                    .set(record).execute();
        }
        try {
            dbConnection.close();
        } catch (SQLException e) {
            logger.error("Failed to close database connection!", e);
        }
    }

}
