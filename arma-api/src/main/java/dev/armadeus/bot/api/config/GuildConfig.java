package dev.armadeus.bot.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface GuildConfig {

    // Command Cooldown
    long getCommandCooldown();
    GuildConfig setCommandCooldown(long cooldown);

    long getPurgeDelay();
    GuildConfig setPurgeDelay(long delay);

    // Command Deletion
    boolean deleteCommandMessages();
    GuildConfig deleteCommandMessages(boolean delete);

    // Prefixes
    Set<String> getPrefixes();
    GuildConfig setPrefixes(Set<String> prefixes);
    GuildConfig addPrefixes(String... prefixes);
    GuildConfig removePrefixes(String... prefixes);

    // Blacklisted Commands
    Set<String> getDisabledCommands();
    GuildConfig setDisabledCommands(Set<String> permissions);
    GuildConfig addDisabledCommand(String... permissions);
    GuildConfig removeDisabledCommand(String... permissions);

    // Blacklisted Commands Per Role
    Set<String> getDisabledCommandsForRole(long roleId);
    GuildConfig setDisabledCommandsForRole(long roleId, Set<String> permissions);
    GuildConfig addDisabledCommandsForRole(long roleId, String... permissions);
    GuildConfig removeDisabledCommandsForRole(long roleId, String... permissions);

    Map<String, CommentedConfig> getMetadata();
    CommentedConfig getMetadata(String key);
    CommentedConfig getMetadataOrInitialize(String key, Consumer<CommentedConfig> config);
    GuildConfig setMetadata(String key, CommentedConfig config);

    Guild getGuild();
}
