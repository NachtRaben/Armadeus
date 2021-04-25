package dev.armadeus.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class GuildConfigImpl implements GuildConfig {

    private transient long guildId;
    private transient CommentedConfig config;

    public GuildConfigImpl(long guildId, CommentedConfig config) {
        this.guildId = guildId;
        this.config = config;
    }

    @Override
    public long getCommandCooldown() {
        return config.getOrElse("commandCooldown", -1);
    }

    @Override
    public GuildConfig setCommandCooldown(long cooldown) {
        config.set("commandCooldown", cooldown);
        return this;
    }

    @Override
    public long getPurgeDelay() {
        return config.getOrElse("purgeDelay", 120);
    }

    @Override
    public GuildConfig setPurgeDelay(long delay) {
        config.set("purgeDelay", delay);
        return this;
    }

    @Override
    public boolean deleteCommandMessages() {
        return config.getOrElse("deleteCommands", true);
    }

    @Override
    public GuildConfig setDeleteCommandMessages(boolean delete) {
        config.set("deleteCommands", delete);
        return this;
    }

    @Override
    public List<String> getPrefixes() {
        return config.getOrElse("prefixes", List.of());
    }

    @Override
    public GuildConfig setPrefixes(Collection<String> prefixes) {
        config.set("prefixes", prefixes);
        return this;
    }

    @Override
    public List<String> getDisabledCommands() {
        return config.getOrElse("disabledCommands", List.of());
    }

    @Override
    public GuildConfig setDisabledCommands(List<String> commands) {
        config.set("disabledCommands", commands);
        return this;
    }

    @Override
    public Map<Long, List<String>> getDisabledCommandsByRole() {
        return config.getOrElse("disabledCommandsPerRole", Map.of());
    }

    @Override
    public GuildConfig setDisabledCommandsByRole(Map<Long, List<String>> disabledCommandsByRole) {
        config.set("disabledCommandsByRole", disabledCommandsByRole);
        return this;
    }

    @Override
    public List<String> getDisabledCommandsFor(long roleId) {
        return config.getOrElse(asList("disabledCommandsByRole", Long.toString(roleId)), List.of());
    }

    @Override
    public GuildConfig setDisabledCommandsFor(long roleId, List<String> commands) {
        config.set(asList("disabledCommandsByRole", Long.toString(roleId)), commands);
        return this;
    }

    @Override
    public Map<String, CommentedConfig> getMetadata() {
        return config.getOrElse("metadata", CommentedConfig.inMemory()).entrySet().stream().collect(Collectors.toMap(UnmodifiableConfig.Entry::getKey, UnmodifiableConfig.Entry::getValue));
    }

    @Override
    public CommentedConfig getMetadata(String key) {
        return config.get(asList("metadata", key));
    }

    public CommentedConfig getMetadataOrInitialize(String key, Consumer<CommentedConfig> initializer) {
        CommentedConfig metaConf = getMetadata(key);
        if(initializer != null && metaConf == null) {
            metaConf = config.createSubConfig();
            initializer.accept(metaConf);
            if(metaConf.isEmpty()) {
                throw new IllegalArgumentException("Configurations cannot be empty!");
            }
            setMetadata(key, metaConf);
        }
        return metaConf;
    }

    @Override
    public GuildConfig setMetadata(String key, CommentedConfig config) {
        this.config.set(asList("metadata", key), config);
        return this;
    }

    public CommentedConfig getConfig() {
        return config;
    }

    @Override
    public Guild getGuild() {
        return ArmaCore.get().getShardManager().getGuildById(guildId);
    }
}
