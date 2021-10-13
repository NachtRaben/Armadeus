package dev.armadeus.discord.moderation;

import co.aikar.commands.CommandManager;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.google.inject.Inject;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.discord.moderation.commands.ModerationCommands;
import dev.armadeus.discord.moderation.commands.UserCommands;
import dev.armadeus.discord.moderation.listeners.ModerationListener;
import dev.armadeus.discord.moderation.util.NotifyAction;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.ArrayList;

@Getter
@Plugin(id = "arma-moderation", name = "Arma-Moderation", version = "0.11", url = "https://armadeus.com", description = "Moderation features.", authors = { "NanoAi" })
public class ArmaModeration {

    public static final NotifyAction notifyAction = new NotifyAction();
    public static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = LoggerFactory.getLogger( ArmaModeration.class);

    @Getter
    @Accessors(fluent = true)
    private static ArmaModeration get;

    @Getter
    @Accessors(fluent = true)
    private static ArmaCore core;

    @Inject
    public ArmaModeration( Velocity core) {
        logger.info("Initializing Arma-Moderation...");
        ArmaModeration.core = (ArmaCore) core;
        get = this;
    }

    @Subscribe
    public void initializeBuilder(DefaultShardManagerBuilder builder) {
        logger.info("Registering event listeners...");
        builder.addEventListeners(new ModerationListener(core));
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        logger.info("Registering Moderation Commands...");
        manager.registerCommand(new ModerationCommands());
        manager.registerCommand(new UserCommands());

        CommentedConfig data = core.armaConfig().getMetadataOrInitialize( "CustomStatus", e -> e.set( "CustomStatus", "Arma-Core #%d" ) );
        String customStatus = data.get( "CustomStatus" );
        core.shardManager().setActivityProvider( value -> Activity.playing( String.format(customStatus, value) ) );
    }

    public Config getConfig(Guild guild) {
        GuildConfig config = core.guildManager().getConfigFor(guild);
        return config.getMetadataOrInitialize("nano-moderation", c -> {
            c.set( "enabled", false );
            c.set( "slowmodeA", 7 );
            c.set( "slowmodeB", 60 );
            c.set( "muteAfterXMessages", 3 );
            c.set( "staffRoleID", 892590384004358225L );
            c.set( "muteRoleID", 894557484310757417L );
            c.set( "verifiedRoleID", 872169571572916275L );
            c.set( "msgChannel", 895211136620830762L );
            c.set( "assignableRoles", new ArrayList<Long>() );
        });
    }

}
