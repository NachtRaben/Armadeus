package dev.armadeus.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ConversionTable;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SaveNotifyConfig extends CommentedConfigWrapper<CommentedConfig> {

    @Getter
    private final long guildId;
    private final AtomicBoolean needsSaved = new AtomicBoolean(false);

    // Children
    private SaveNotifyConfig parent;

    public SaveNotifyConfig(long guildId, CommentedConfig config) {
        super(config);
        this.guildId = guildId;
    }

    private SaveNotifyConfig(SaveNotifyConfig parent, CommentedConfig config) {
        super(config);
        this.parent = parent;
        this.guildId = parent.guildId;
    }

    @Override
    public <T> T get(String path) {
        T result = super.get(path);
        if(result instanceof CommentedConfig) {
            result = (T) new SaveNotifyConfig(this, (CommentedConfig)result);
        }
        return result;
    }

    @Override
    public <T> T get(List<String> path) {
        T result = super.get(path);
        if(result instanceof CommentedConfig) {
            result = (T) new SaveNotifyConfig(this, (CommentedConfig)result);
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
    public String setComment(List<String> path, String comment) {
        String result = super.setComment(path, comment);
        save();
        return result;
    }

    @Override
    public String removeComment(List<String> path) {
        String result = super.removeComment(path);
        save();
        return result;
    }

    @Override
    public Map<String, Object> valueMap() {
        return new ObservedMap<>(super.valueMap(), this::save);
    }

    @Override
    public Map<String, String> commentMap() {
        return new ObservedMap<>(super.commentMap(), this::save);
    }

    public void save() {
        if(parent != null) {
            parent.save();
            return;
        }
        needsSaved.set(true);
    }

    public AtomicBoolean needsSaved() {

        return needsSaved;
    }

    public SaveNotifyConfig createSubConfig() {
        return new SaveNotifyConfig(this, super.createSubConfig());
    }

}
