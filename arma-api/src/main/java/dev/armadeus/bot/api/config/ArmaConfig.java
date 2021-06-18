package dev.armadeus.bot.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ArmaConfig {

    Logger logger = LoggerFactory.getLogger(ArmaConfig.class);

    String getToken();
    List<Integer> getShards();
    Integer getShardsTotal();
    Set<Long> getOwnerIds();
    Set<Long> getDeveloperIds();
    Set<String> getDefaultPrefixes();
    boolean isDatabaseEnabled();

    Map<String, CommentedConfig> getMetadata();
    CommentedConfig getMetadata(String key);
    CommentedConfig getMetadataOrInitialize(String key, Consumer<CommentedConfig> config);
    ArmaConfig setMetadata(String key, CommentedConfig config);

    Connection createDatabaseConnection();
}
