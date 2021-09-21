package dev.armadeus.core.util;

import com.electronwill.nightconfig.core.Config;

public class ConfigUtil {

    public static void merge(Config source, Config dest) {
        for(Config.Entry entry : source.entrySet()) {
            if(!dest.valueMap().containsKey(entry.getKey())) {
                // Fully merge the two children
                dest.valueMap().put(entry.getKey(), entry.getValue());
                continue;
            }
            // Merge any children configs
            if(entry.getValue() instanceof Config && dest.getRaw(entry.getKey()) instanceof Config) {
                merge(entry.getValue(), dest.getRaw(entry.getKey()));
            }
        }
    }

}
