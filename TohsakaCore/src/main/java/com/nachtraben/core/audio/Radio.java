package com.nachtraben.core.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.AudioUtil;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public enum Radio {

    // TODO: Move this to abstract classes with triggers/events for listeners/current track info.
    SPOTIFY("Spotify Radio", "NachtRaben", "http://localhost:13375", null),
    HIVE("Hive Radio", "Hive365", "http://stream.hive365.co.uk:8088/live",
            new EmbedBuilder().setColor(Color.YELLOW)
                    .setTitle("Radio provided by Hive365.", "https://hive365.co.uk/")
                    .setDescription("Non-stop music guaranteed to get ya buzzin'")
                    .setThumbnail("https://hive365.co.uk/img/team/beekeeper.jpg")
                    .build()),
    MOE("LISTEN.moe", "LISTEN.moe", "http://listen.moe/stream.m3u", null),
    MONSTERCAT("Monstercat Radio", "dooley_labs", (String) Tohsaka.getInstance().getConfig().getMetadata().get("monstercat_radio_address"), null),
    ROOKERY("Rookery Radio", "Coolguy3289", "http://stream.rookeryradio.com:8088/live", null),
    NOLIFE("NoLife-Radio", "NoLife", "http://nolife-radio.com/radio/NoLife-radio.m3u", null);
    private static final Logger log = LoggerFactory.getLogger(Radio.class);

    private String title;
    private String artist;
    private String url;
    private MessageEmbed embed;

    Radio(String title, String artist, String url, MessageEmbed embed) {
            this.title = title;
            this.artist = artist;
            this.url = url;
            this.embed = embed;
    }

    public void playRadio(GuildCommandSender sender) {
        GuildMusicManager manager = sender.getDbot().getGuildManager().getConfigurationFor(sender.getGuild()).getMusicManager();
        manager.getPlayerManager().loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(sender);
                AudioUtil.transform(track.getInfo(), title, artist);
                if(embed != null)
                    sender.sendMessage(ChannelTarget.MUSIC, embed);
                manager.getScheduler().play(track);
                manager.getScheduler().setRepeatTrack(true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.debug("Somehow loaded a playlist when trying to play, " + this.toString());
                sender.sendMessage(ChannelTarget.MUSIC, "Something went wrong while playing the radio, the bot author has been notified. FOUND_PLAYLIST");
            }

            @Override
            public void noMatches() {
                log.debug("Couldn't load radio when attempt to play, " + this.toString());
                sender.sendMessage("Something went wrong while playing the radio, the bot author has been notified. NO_MATCH");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sender.sendMessage("Failed to play `" + name() + " Radio`, is it offline?");
            }
        });
    }

    public static Radio forName(String s) {
        for(Radio r : values()) {
            if(s.equalsIgnoreCase(r.toString()))
                return r;
        }
        return null;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Radio getByAddress(String uri) {
        for (Radio rad : values())
            if (rad.url.equalsIgnoreCase(uri))
                return rad;
        return null;
    }
}
