package dev.armadeus.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.core.ArmaCoreImpl;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public int getCommandCooldown() {
        return config.getOrElse("commandCooldown", -1);
    }

    @Override
    public GuildConfig setCommandCooldown(int cooldown) {
        config.set("commandCooldown", cooldown);
        return this;
    }

    @Override
    public int getPurgeDelay() {
        return config.getOrElse("purgeDelay", 120);
    }

    @Override
    public GuildConfig setPurgeDelay(int delay) {
        config.set("purgeDelay", delay);
        return this;
    }

    @Override
    public boolean isDevGuild() {
        return config.getOrElse("devGuild", false);
    }

    @Override
    public GuildConfig setDevGuild(boolean enabled) {
        config.set("devGuild", enabled);
        return this;
    }

    @Override
    public boolean deleteCommandMessages() {
        return config.getOrElse("deleteCommands", true);
    }

    @Override
    public GuildConfig deleteCommandMessages(boolean delete) {
        config.set("deleteCommands", delete);
        return this;
    }

    @Override
    public Set<String> getPrefixes() {
        return new HashSet<>(config.getOrElse("prefixes", List.of()));
    }

    @Override
    public GuildConfig setPrefixes(Set<String> prefixes) {
        config.set("prefixes", new ArrayList<>(prefixes));
        return this;
    }

    @Override
    public GuildConfig addPrefixes(String... prefixes) {
        Set<String> pre = getPrefixes();
        pre.addAll(Arrays.asList(prefixes));
        setPrefixes(pre);
        return this;
    }

    @Override
    public GuildConfig removePrefixes(String... prefixes) {
        Set<String> pre = getPrefixes();
        Arrays.asList(prefixes).forEach(pre::remove);
        setPrefixes(pre);
        return this;
    }

    @Override
    public Set<String> getDisabledCommands() {
        return new HashSet<>(config.getOrElse("disabledCommands", List.of()));
    }

    @Override
    public GuildConfig setDisabledCommands(Set<String> permissions) {
        config.set("disabledCommands", new ArrayList<>(permissions));
        return this;
    }

    @Override
    public GuildConfig addDisabledCommand(String... permissions) {
        Set<String> disabled = getDisabledCommands();
        disabled.addAll(Arrays.asList(permissions));
        setDisabledCommands(disabled);
        return this;
    }

    @Override
    public GuildConfig removeDisabledCommand(String... permissions) {
        Set<String> disabled = getDisabledCommands();
        Arrays.asList(permissions).forEach(disabled::remove);
        setDisabledCommands(disabled);
        return this;
    }

    @Override
    public Set<String> getDisabledCommandsForRole(long roleId) {
        return new HashSet<>(config.getOrElse(asList("disabledCommandsByRole", Long.toString(roleId)), List.of()));
    }

    @Override
    public GuildConfig setDisabledCommandsForRole(long roleId, Set<String> permissions) {
        config.set(asList("disabledCommandsByRole", Long.toString(roleId)), new ArrayList<>(permissions));
        return this;
    }

    @Override
    public GuildConfig addDisabledCommandsForRole(long roleId, String... permissions) {
        Set<String> disabled = getDisabledCommandsForRole(roleId);
        disabled.addAll(Arrays.asList(permissions));
        setDisabledCommandsForRole(roleId, disabled);
        return this;
    }

    @Override
    public GuildConfig removeDisabledCommandsForRole(long roleId, String... permissions) {
        Set<String> disabled = getDisabledCommandsForRole(roleId);
        Arrays.asList(permissions).forEach(disabled::remove);
        if(disabled.isEmpty()) {
            config.remove(asList("disabledCommandsByRole", Long.toString(roleId)));
            return this;
        }
        setDisabledCommandsForRole(roleId, disabled);
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
        return ArmaCoreImpl.get().shardManager().getGuildById(guildId);
    }
}
