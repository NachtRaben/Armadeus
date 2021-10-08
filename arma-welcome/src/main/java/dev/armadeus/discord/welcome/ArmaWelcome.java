package dev.armadeus.discord.welcome;

import co.aikar.commands.CommandManager;
import com.electronwill.nightconfig.core.Config;
import com.google.inject.Inject;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.discord.welcome.commands.WelcomeCommand;
import dev.armadeus.discord.welcome.listeners.WelcomeListener;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Plugin(id = "arma-welcome", name = "Arma-Welcome", version = "0.1", url = "https://armadeus.com", description = "Default welcome implementation", authors = { "NachtRaben" })
public class ArmaWelcome {

    private static final Logger logger = LoggerFactory.getLogger(ArmaWelcome.class);
    @Getter
    @Accessors(fluent = true)
    private static ArmaWelcome get;

    @Getter
    @Accessors(fluent = true)
    private static ArmaCore core;

    @Inject
    public ArmaWelcome(Velocity core) {
        logger.info("Initializing Arma-Welcome...");
        ArmaWelcome.core = (ArmaCore) core;
        get = this;
    }

    @Subscribe
    public void initializeBuilder(DefaultShardManagerBuilder builder) {
        logger.info("Registering event listeners...");
        builder.addEventListeners(new WelcomeListener(core));
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        logger.info("Registered command WelcomeCommand");
        manager.registerCommand(new WelcomeCommand());
    }

    public Config getConfig(Guild guild) {
        GuildConfig config = core.guildManager().getConfigFor(guild);
        return config.getMetadataOrInitialize("arma-welcome", c -> {
            c.set("enabled", false);
            c.set("dm", true);
            c.set("message", "Welcome %username% to %guild%!");
        });
    }

}
