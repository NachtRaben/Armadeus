package dev.armadeus.core.configuration;

import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildManager;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ConfigUtil;
import dev.armadeus.core.util.DiscordReference;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.armadeus.core.util.ConfigUtil.*;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;

@Getter
public class GuildConfig {

    private final transient static Logger logger = LogManager.getLogger();
    public transient static final Path GUILD_DIR = Path.of("guilds");
    private static final CommentedConfigurationNode defaults = CommentedConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(true));

    static {
        try {
            defaults.node("deleteCommands").act(act -> {
                act.commentIfAbsent("Should we delete command messages");
                act.getBoolean(true);
            });
            defaults.node("prefixes").act(act -> {
                act.commentIfAbsent("Guild specific command prefixes. These will override the global ones.");
                act.getList(String.class, BotConfig.get().getGlobalPrefixes());
            });
            defaults.node("disabledCommands").act(act -> {
                act.commentIfAbsent("List of disabled commands");
                CommentedConfigurationNode value = act.node("unknown");
                value.getList(Long.class, List.of(-1L, -1L));
            });
            defaults.node("isBlacklist").act(act -> {
                act.commentIfAbsent("Should disabled commands act as a blackist");
                act.getBoolean(true);
            });
            defaults.node("metadata").act(act -> {
                act.commentIfAbsent("Various guild metadata");
                CommentedConfigurationNode volume = act.node("volume");
                volume.getInt(100);
            });
            defaults.node("cooldown").act(act -> {
                act.commentIfAbsent("Guild command cooldown");
                act.getInt(-1);
            });
            defaults.node("messageTimeout").act(act -> {
                act.commentIfAbsent("Bot message timeout");
                act.getLong(DiscordUser.getDefaultMessageTimeout());
            });
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    @Getter(value = AccessLevel.NONE)
    private final HoconConfigurationLoader loader;
    @Getter(value = AccessLevel.NONE)
    private CommentedConfigurationNode root;

    private final GuildManager manager;
    private transient GuildMusicManager musicManager;
    private final long guildId;
    private final DiscordReference<Guild> guild;

    public GuildConfig(GuildManager manager, Long guildId) {
        this.guildId = guildId;
        this.manager = manager;
        this.guild = new DiscordReference<>(DiscordBot.get().getShardManager().getGuildById(this.guildId), id -> DiscordBot.get().getShardManager().getGuildById(id));
        this.root = defaults;
        Path newPath = GUILD_DIR.resolve(guildId + ".conf");
        this.loader = HoconConfigurationLoader.builder()
                .defaultOptions(opts -> opts.shouldCopyDefaults(true))
                .path(newPath)
                .build();
    }

    public GuildConfig load() {
        Path legacy = GUILD_DIR.resolve(guildId + ".json");
        ConfigurationNode legacyNode = null;
        if (Files.exists(legacy)) {
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().defaultOptions(ConfigurationOptions.defaults().shouldCopyDefaults(true))
                    .path(legacy)
                    .build();
            try {
                legacyNode = loader.load();
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        }

        try {
            root = loader.load();
            if (legacyNode != null) {
                root.mergeFrom(legacyNode);
                save();
            }
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
        return this;
    }

    public GuildConfig save() {
        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
        return this;
    }

    public GuildMusicManager getMusicManager() {
        if (musicManager == null)
            return musicManager = new GuildMusicManager(getGuild());
        return musicManager;
    }

    public Long getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return guild.resolve();
    }

    public boolean shouldDeleteCommands() {
        return root.node("deleteCommands").getBoolean();
    }

    public void setDeleteCommands(boolean delete) {
        setNode(root.node("deleteCommands"), Boolean.class, delete);
    }

    public List<String> getPrefixes() {
        return getList(root.node("prefixes"), String.class);
    }

    public void setPrefixes(List<String> prefixes) {
        setList(root.node("prefixes"), String.class, prefixes.toArray(new String[0]));
    }

    public void addPrefix(String prefix) {
        List<String> prefixes = getPrefixes();
        prefixes.add(prefix);
        setPrefixes(prefixes);
    }

    public Map<String, Set<Long>> getDisabledCommands() {
        return getMap(root, TypeToken.get(parameterizedClass(Set.class, Long.class)));
    }

    public void setDisabledCommands(Map<String, Set<Long>> commands) {
        ConfigUtil.setMap(root.node("disabledCommands"), TypeToken.get(parameterizedClass(Set.class, Long.class)), commands);
    }

    public boolean isBlacklist() {
        return root.node("isBlacklist").getBoolean();
    }

    public void setBlacklist(boolean blacklist) {
        setNode(root.node("isBlacklist"), Boolean.class, blacklist);
    }

    public void addDisabledCommand(String command, long groupId) {
        Map<String, Set<Long>> disabled = getDisabledCommands();
        Set<Long> disabledRoles = disabled.computeIfAbsent(command, l -> new HashSet<>());
        disabledRoles.add(groupId);
        setDisabledCommands(disabled);
    }

    public void removeDisabledCommand(String command, long groupId) {
        Map<String, Set<Long>> disabled = getDisabledCommands();
        Set<Long> disabledRoles = disabled.get(command);
        if (disabledRoles == null)
            return;
        disabledRoles.remove(groupId);
        setDisabledCommands(disabled);
    }

    public Map<String, String> getMetadata() {
        return getMap(root.node("metadata"), TypeToken.get(String.class));
    }

    public void setMetadata(Map<String, String> metadata) {
        setMap(root.node("metadata"), TypeToken.get(TypeFactory.parameterizedClass(Map.class, String.class, String.class)), metadata);
    }

    public long getCommandCooldown() {
        return root.node("cooldown").getLong();
    }

    public void setCooldown(long cooldown) {
        setNode(root.node("cooldown"), Long.class, cooldown);
    }

    public boolean hasCommandCooldown() {
        return getCommandCooldown() > 0;
    }

    public long getMessageTimeout() {
        return root.node("messageTimeout").getLong();
    }

    public void setMessageTimeout(long timeout) {
        setNode(root.node("messageTimeout"), Long.class, timeout);
    }

    public void setVolume(float volume) {
        Map<String, String> metadata = getMetadata();
        metadata.put("volume", String.valueOf(volume));
        save();
    }

    public float getVolume() {
        Map<String, String> metadata = getMetadata();
        metadata.computeIfAbsent("volume", k -> "1.0");
        float metaVolume = Float.parseFloat(metadata.get("volume"));
        if (metaVolume > 1.0f) {
            setVolume(metaVolume /= 100.0f);
        }
        return metaVolume;
    }
}