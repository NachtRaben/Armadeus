package com.nachtraben.core.managers;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.configuration.RedisGuildConfig;
import com.nachtraben.pineappleslice.redis.Redis;
import com.nachtraben.pineappleslice.redis.RedisProvider;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuildManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);

    private DiscordBot dbot;
    private RedisProvider provider;
    private Redis connection;
    private boolean useRedis = false;
    private Map<Long, GuildConfig> configs;

    public GuildManager(DiscordBot dbot) {
        this.dbot = dbot;
        configs = new HashMap<>();
    }

    public GuildManager(DiscordBot dbot, RedisProvider provider) {
        this.dbot = dbot;
        this.provider = provider;
        useRedis = true;
        configs = new HashMap<>();
    }

    public GuildConfig getConfigurationFor(Long guildID) {
        GuildConfig config = configs.get(guildID);
        if(config == null) {
            if(useRedis) {
                configs.put(guildID, config = new RedisGuildConfig(this, guildID).load());
            } else {
                configs.put(guildID, config = new GuildConfig(this, guildID).load());
            }
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

    public DiscordBot getDbot() {
        return dbot;
    }

    public Redis getConnection() {
        if(provider == null)
            throw new IllegalStateException("RedisProvider cannot be null!");

        if(connection == null) {
            return connection = provider.getSession(14);
        }
        return connection;
    }

}
