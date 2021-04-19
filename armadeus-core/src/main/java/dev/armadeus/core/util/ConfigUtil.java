package dev.armadeus.core.util;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigUtil {

    public static ConfigurationNode setNode(ConfigurationNode node, Type type, Object value) {
        try {
            return node.set(type, value);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return node;
    }

    public static <T> ConfigurationNode setList(ConfigurationNode node, Class<T> type, T... values) {
        try {
            return node.setList(type, List.of(values));
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return node;
    }

    public static <T> List<T> getList(ConfigurationNode node, Class<T> type) {
        try {
            return node.getList(type);
        } catch (SerializationException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static <T> Map<String, T> getMap(ConfigurationNode node, TypeToken<?> type) {
        try {
            Map<String, T> result = new HashMap<>();
            Map<Object, ? extends ConfigurationNode> children = node.childrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : children.entrySet()) {
                result.put((String) entry.getKey(), (T) entry.getValue().get(type));
            }
            return result;
        } catch (SerializationException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public static <T> ConfigurationNode setMap(ConfigurationNode node, TypeToken<?> type, Map<String, T> value) {
        try {
            for (Map.Entry<String, ?> entry : value.entrySet()) {
                ConfigurationNode sub = node.node(entry.getKey());
                sub.set(type.getType(), entry.getValue());
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return node;
    }
}
