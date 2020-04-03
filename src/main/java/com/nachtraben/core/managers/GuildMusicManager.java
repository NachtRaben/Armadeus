package com.nachtraben.core.managers;

import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.tohsaka.Tohsaka;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GuildMusicManager {

    private static final Logger logger = LoggerFactory.getLogger(GuildMusicManager.class);

    private final SnowflakeReference<Guild> guild;
    private final JdaLink link;
    private final IPlayer player;
    private final TrackScheduler scheduler;

    public GuildMusicManager(Guild guild) {
        checkNotNull(guild);
        this.guild = new SnowflakeReference<>(guild, id -> Tohsaka.getInstance().getShardManager().getGuildById(id));
        this.link = Tohsaka.getInstance().getLavalink().getLink(guild);
        this.player = link.getPlayer();
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
    }

    public Guild getGuild() {
        return guild.resolve();
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

}
