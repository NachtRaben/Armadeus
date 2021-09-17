package dev.armadeus.bot.api;

import co.aikar.commands.ArmaCommandManager;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.scheduler.Scheduler;
import dev.armadeus.bot.api.config.ArmaConfig;
import dev.armadeus.bot.api.guild.GuildManager;
import net.dv8tion.jda.api.sharding.ShardManager;

public interface ArmaCore extends Velocity {

    // TODO: Replace with abstract class again
    GuildManager guildManager();

    ArmaConfig armaConfig();

    // VelocityUtils
    Scheduler scheduler();

    PluginManager pluginManager();

    EventManager eventManager();

    ArmaCommandManager commandManager();

    ShardManager shardManager();

    void shutdown();
}
