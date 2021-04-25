package dev.armadeus.bot.api.guild;

import dev.armadeus.bot.api.config.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;

public interface GuildManager {

    GuildConfig getConfigFor(Guild guild);
    GuildConfig getConfigFor(long guildId);
    void shutdown();

}
