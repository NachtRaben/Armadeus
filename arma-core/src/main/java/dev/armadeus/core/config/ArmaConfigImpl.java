package dev.armadeus.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.armadeus.bot.api.config.ArmaConfig;
import dev.armadeus.core.ArmaCoreImpl;
import dev.armadeus.core.util.ConfigUtil;
import lombok.Getter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

@Getter
public class ArmaConfigImpl implements ArmaConfig {

    private transient CommentedConfig config;

    public ArmaConfigImpl(CommentedConfig config) {
        this.config = config;
    }

    @Override
    public String getToken() {
        return ArmaCoreImpl.getOptions().has("token") ? (String) ArmaCoreImpl.getOptions().valueOf("token") : config.get(asList("arma-core", "token"));
    }

    @Override
    public List<Integer> getShards() {
        return config.get(asList("arma-core", "shards"));
    }

    @Override
    public Integer getShardsTotal() {
        return config.get(asList("arma-core", "shardsTotal"));
    }

    @Override
    public List<Long> getOwnerIds() {
        return config.get(asList("arma-core", "ownerIds"));
    }

    @Override
    public List<Long> getDeveloperIds() {
        Set<Long> joined = new HashSet<>();
        joined.addAll(config.get(asList("arma-core", "developerIds")));
        joined.addAll(config.get(asList("arma-core", "ownerIds")));
        return new ArrayList<>(joined);
    }

    @Override
    public List<String> getDefaultPrefixes() {
        return config.get(asList("arma-core", "defaultPrefixes"));
    }

    public boolean isDatabaseEnabled() {
        return config.get(asList("database", "enabled"));
    }

    public Database getDatabaseInfo() {
        return new ObjectConverter().toObject(config.get("database"), Database::new);
    }

    public static ArmaConfig read(Path path) {
        URL defaultConfigLocation = ArmaConfigImpl.class.getClassLoader().getResource("configs/arma-core.toml");
        checkNotNull(defaultConfigLocation, "Default Arma-Core configuration does not exist");

        CommentedConfig defaults = TomlFormat.instance().createParser().parse(defaultConfigLocation);

        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .preserveInsertionOrder()
                .defaultData(defaultConfigLocation)
                .autosave()
                .sync()
                .build();
        config.load();

        ConfigUtil.merge(defaults, config);
        return new ArmaConfigImpl(config);
    }

    @Getter
    public static class Database {
        boolean enabled;
        String username = "postgres";
        String password = "password";
        String uri = "jdbc:postgresql://localhost:5432/postgres";
    }

}