package dev.armadeus.bot.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GuildConfig {

    // Command Cooldown
    long getCommandCooldown();
    GuildConfig setCommandCooldown(long cooldown);

    long getPurgeDelay();
    GuildConfig setPurgeDelay(long delay);

    // Command Deletion
    boolean deleteCommandMessages();
    GuildConfig setDeleteCommandMessages(boolean delete);

    // Prefixes
    List<String> getPrefixes();
    GuildConfig setPrefixes(Collection<String> prefixes);

    // Blacklisted Commands
    List<String> getDisabledCommands();
    GuildConfig setDisabledCommands(List<String> commands);

    // Blacklisted Commands Per Role
    Map<Long, List<String>> getDisabledCommandsByRole();
    GuildConfig setDisabledCommandsByRole(Map<Long, List<String>> disabledCommandsByRole);
    List<String> getDisabledCommandsFor(long roleId);
    GuildConfig setDisabledCommandsFor(long roleId, List<String> commands);

    Map<String, CommentedConfig> getMetadata();
    CommentedConfig getMetadata(String key);
    CommentedConfig getMetadataOrInitialize(String key, Consumer<CommentedConfig> config);
    GuildConfig setMetadata(String key, CommentedConfig config);

    GuildConfig save();
    Guild getGuild();
}
