package com.nachtraben.core.managers;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuildManager {

    private static final Object CONNECTION_LOCK = new Object();
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);

    private final DiscordBot dbot;
    private final Map<Long, GuildConfig> configs;

    public GuildManager(DiscordBot dbot) {
        this.dbot = dbot;
        configs = new HashMap<>();
    }

    public GuildConfig getConfigurationFor(Long guildID) {
        GuildConfig config = configs.get(guildID);
        if (config == null) {
            configs.put(guildID, config = new GuildConfig(this, guildID).load());
        }
        return config;
    }

    public GuildConfig getConfigurationFor(Guild guild) {
        return getConfigurationFor(guild.getIdLong());
    }

    public DiscordBot getDbot() {
        return dbot;
    }

}
