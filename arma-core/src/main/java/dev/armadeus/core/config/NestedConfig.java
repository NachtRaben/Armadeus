package dev.armadeus.core.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NestedConfig extends ConfigWrapper<Config> {

    @Getter
    private final long guildId;
    private final AtomicBoolean needsSaved = new AtomicBoolean(false);

    // Children
    private NestedConfig parent;

    public NestedConfig(long guildId, Config internal) {
        super(internal);
        this.guildId = guildId;
    }

    private NestedConfig(NestedConfig parent, Config config) {
        super(config);
        this.parent = parent;
        this.guildId = parent.guildId;
    }

    @Override
    public <T> T get(String path) {
        T result = super.get(path);
        if(result instanceof Config) {
            //noinspection unchecked
            result = (T) new NestedConfig(this, (Config)result);
        }
        return result;
    }

    @Override
    public <T> T get(List<String> path) {
        T result = super.get(path);
        if(result instanceof Config) {
            //noinspection unchecked
            result = (T) new NestedConfig(this, (Config)result);
        }
        return result;
    }

    @Override
    public <T> T set(List<String> path, Object value) {
        T result = super.set(path, value);
        save();
        return result;
    }

    @Override
    public boolean add(List<String> path, Object value) {
        boolean result = super.add(path, value);
        save();
        return result;
    }

    @Override
    public <T> T remove(List<String> path) {
        T result = super.remove(path);
        save();
        return result;
    }

    @Override
    public Map<String, Object> valueMap() {
        return new ObservedMap<>(super.valueMap(), this::save);
    }

    public void save() {
        if(parent != null) {
            parent.save();
            return;
        }
        if(config instanceof FileConfig) {
            ((FileConfig)config).save();
        } else {
            needsSaved.set(true);
        }
    }

    public AtomicBoolean needsSaved() {

        return needsSaved;
    }

    public NestedConfig createSubConfig() {
        return new NestedConfig(this, super.createSubConfig());
    }

}
