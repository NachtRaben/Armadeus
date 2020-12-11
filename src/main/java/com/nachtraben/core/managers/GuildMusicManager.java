package com.nachtraben.core.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.audio.TrackScheduler;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GuildMusicManager {

    float[] bands = new float[]{0.1f, 0.05f, 0.04f, 0.03f, 0f, -0.02f, -0.03f, -0.05f, -0.03f, -0.02f, 0f, 0.03f, 0.04f, 0.05f, 0.1f};

    private static final Logger logger = LoggerFactory.getLogger(GuildMusicManager.class);

    private final Guild guild;
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;

    public GuildMusicManager(Guild guild) {
        checkNotNull(guild);
        this.guild = guild;
        this.link = Armadeus.getInstance().getLavalink().getLink(guild);
        this.player = link.getPlayer();
        this.player.getFilters().setVolume(1.0f).commit();
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
        JsonObject payload = new JsonObject();
        payload.addProperty("op", "equalizer");
        payload.addProperty("guildId", link.getGuildId());
        JsonArray bands = new JsonArray();
        for (int i = 0; i < this.bands.length; i++) {
            JsonObject band = new JsonObject();
            band.addProperty("band", i);
            band.addProperty("gain", this.bands[i] * 0.75f);
            bands.add(band);
        }
        payload.add("bands", bands);
        Objects.requireNonNull(link.getNode(true)).send(payload.toString());
    }

}
