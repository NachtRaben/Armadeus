package dev.armadeus.core.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {

//    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    public static void merge(CommentedConfig source, CommentedConfig dest) {
        for(CommentedConfig.Entry entry : source.entrySet()) {
            if(!dest.valueMap().containsKey(entry.getKey())) {
                // Fully merge the two children
//                logger.warn("Adding missing child {} {}", entry.getKey(), entry.<Object>getValue());
                dest.valueMap().put(entry.getKey(), entry.getValue());
                dest.commentMap().put(entry.getKey(), entry.getComment());
                continue;
            }
            // Check for missing config
            if(entry.getComment() != null && (!dest.getComments().containsKey(entry.getKey()) || !dest.commentMap().get(entry.getKey()).equals(entry.getComment()))) {
//                logger.warn("Adding missing comment {} \"{}\"", entry.getKey(), entry.getComment());
                dest.commentMap().put(entry.getKey(), entry.getComment());
            }
            // Merge any children configs
            if(entry.getValue() instanceof CommentedConfig && dest.getRaw(entry.getKey()) instanceof CommentedConfig) {
                merge(entry.getValue(), dest.getRaw(entry.getKey()));
            }
        }
    }

}
