package com.nachtraben.core.managers;

import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.tohsaka.Tohsaka;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GuildMusicManager {

    float[] bands = new float[]{0.1f, 0.05f, 0.04f, 0.03f, 0f, -0.02f, -0.03f, -0.05f, -0.03f, -0.02f, 0f, 0.03f, 0.04f, 0.05f, 0.1f};

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
        this.player.setVolume(50);
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
        JSONObject payload = new JSONObject();
        payload.put("op", "equalizer");
        payload.put("guildId", link.getGuildId());
        JSONArray bands = new JSONArray();
        for (int i = 0; i < this.bands.length; i++) {
            JSONObject band = new JSONObject();
            band.put("band", i);
            band.put("gain", this.bands[i]);
            bands.put(i, band);
        }
        payload.put("bands", bands);
        Objects.requireNonNull(link.getNode(true)).send(payload.toString());
    }

    public Guild getGuild() {
        return guild.resolve();
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

}
