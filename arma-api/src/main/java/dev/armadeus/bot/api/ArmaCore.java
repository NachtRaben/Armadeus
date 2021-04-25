package dev.armadeus.bot.api;

import co.aikar.commands.CommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.scheduler.Scheduler;
import dev.armadeus.bot.api.config.ArmaConfig;
import dev.armadeus.bot.api.guild.GuildManager;
import net.dv8tion.jda.api.sharding.ShardManager;

public abstract class ArmaCore {
    public static Gson GENERAL_GSON = new GsonBuilder().create();
    private static ArmaCore instance;

    protected ArmaCore() {
        if(instance != null) {
            throw new IllegalStateException("ArmaCore is already initialized");
        }
        instance = this;
    }

    public abstract GuildManager getGuildManager();
    public abstract ArmaConfig getArmaConfig();
    public abstract Scheduler getScheduler();
    public abstract PluginManager getPluginManager();
    public abstract EventManager getEventManager();
    public abstract CommandManager getCommandManager();
    public abstract ShardManager getShardManager();

    public static ArmaCore get() {
        return instance;
    }

}
