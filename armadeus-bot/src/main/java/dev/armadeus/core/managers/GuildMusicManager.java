package dev.armadeus.core.managers;

import dev.armadeus.bot.Armadeus;
import dev.armadeus.core.audio.TrackScheduler;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GuildMusicManager {

    private static final Logger logger = LogManager.getLogger();
    private final Guild guild;
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;
    public Map<Long, Future<?>> listeners = new HashMap<>();
    float[] bands = new float[]{0.1f, 0.05f, 0.04f, 0.03f, 0f, -0.02f, -0.03f, -0.05f, -0.03f, -0.02f, 0f, 0.03f, 0.04f, 0.05f, 0.1f};

    public GuildMusicManager(Guild guild) {
        checkNotNull(guild);
        this.guild = guild;
        this.link = Armadeus.getInstance().getLavalink().getLink(guild);
        this.player = link.getPlayer();
        this.player.getFilters().setVolume(1.0f).commit();
//        this.player.getFilters().setTimescale(new Timescale().setPitch(1.25f).setRate(1.0f).setSpeed(1.0f)).commit();
//        this.player.getFilters().setKaraoke(new Karaoke()).commit();
        for (int i = 0; i < this.bands.length; i++) {
            this.player.getFilters().setBand(i, this.bands[i] * 0.75f).commit();
        }
        this.scheduler = new TrackScheduler(this);
        player.addListener(scheduler);
    }

}
