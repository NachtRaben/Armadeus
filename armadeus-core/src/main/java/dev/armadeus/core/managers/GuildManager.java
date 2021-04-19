package dev.armadeus.core.managers;

import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.configuration.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GuildManager {

    private static final Object CONNECTION_LOCK = new Object();
    private static final Logger logger = LogManager.getLogger();

    private final DiscordBot dbot;
    private final Map<Long, GuildConfig> configs;

    public GuildManager(DiscordBot dbot) {
        this.dbot = dbot;
        configs = new HashMap<>();
    }

    public GuildConfig getConfigurationFor(long guildID) {
        return configs.computeIfAbsent(guildID, __ -> new GuildConfig(this, guildID).load());
    }

    public GuildConfig getConfigurationFor(Guild guild) {
        return getConfigurationFor(guild.getIdLong());
    }

    public DiscordBot getDbot() {
        return dbot;
    }

}
