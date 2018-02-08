package com.nachtraben.core.managers;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.configuration.RedisGuildConfig;
import com.nachtraben.core.util.RedisUtil;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuildManager {

    private static final Logger log = LoggerFactory.getLogger(GuildManager.class);

    private DiscordBot dbot;
    private Map<Long, GuildConfig> configs;

    public GuildManager(DiscordBot dbot) {
        this.dbot = dbot;
        configs = new HashMap<>();
    }

    public GuildConfig getConfigurationFor(Long guildID) {
        GuildConfig config = configs.get(guildID);
        if(config == null) {
            if (RedisUtil.isEnabled())
                configs.put(guildID, config = new RedisGuildConfig(this, guildID).load());
            else
                configs.put(guildID, config = new GuildConfig(this, guildID).load());
        }
        return config;
    }

    public GuildConfig getConfigurationFor(Guild guild) {
        return getConfigurationFor(guild.getIdLong());
    }

    public void saveConfigurations() {
        for(Map.Entry<Long, GuildConfig> config : configs.entrySet()) {
            config.getValue().save();
        }
    }

    // TODO: Rework persistence
//    public void savePersistInformation() {
//        if(!configs.isEmpty())
//            log.debug("Saving persist information.");
//        for(GuildConfig c : configs.values())
//            c.savePersistInfo();
//    }
//
//    public void loadPersistInformation() {
//        for(File f : GuildConfig.PERSIST_DIR.listFiles(new PatternFilenameFilter(".*.persist"))) {
//            try {
//                Long id = Long.parseLong(f.getName().replace(".persist", ""));
//                GuildConfig config = getConfigurationFor(id);
//                config.loadPersistInfo(f);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public DiscordBot getDbot() {
        return dbot;
    }



}
