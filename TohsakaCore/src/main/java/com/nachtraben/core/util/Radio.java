package com.nachtraben.core.util;

import com.nachtraben.core.audio.MusicRequestor;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum Radio {

    SPOTIFY("Spotify Radio", "NachtRaben", "http://nachtvps:13375", null),
    HIVE("Hive Radio", "Hive365", "http://stream.hive365.co.uk:8088/live",
            new EmbedBuilder().setColor(Color.YELLOW)
                    .setTitle("Radio provided by Hive365.", "https://hive365.co.uk/")
                    .setDescription("Non-stop music guaranteed to get ya buzzin'")
                    .build());

    private static final Logger LOGGER = LoggerFactory.getLogger(Radio.class);
    private static Field titleField;
    private static Field artistField;
    static {
        try {
            Class clz = AudioTrackInfo.class;
            titleField = clz.getDeclaredField("title");
            titleField.setAccessible(true);
            artistField = clz.getDeclaredField("author");
            artistField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            modifiersField.setInt(titleField, titleField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(artistField, artistField.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.warn("Failed to modify AudioTrackInfo fields!", e);
        }
    }

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
                modifyTrackInfo(track.getInfo(), title, artist);
                if(embed != null)
                    sender.sendMessage(ChannelTarget.MUSIC, embed);
                manager.getScheduler().play(track);
                manager.getScheduler().setRepeatTrack(true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                LOGGER.debug("Somehow loaded a playlist when trying to play, " + this.toString());
                sender.sendMessage(ChannelTarget.MUSIC, "Something went wrong while playing the radio, the bot author has been notified. FOUND_PLAYLIST");
            }

            @Override
            public void noMatches() {
                LOGGER.debug("Couldn't load radio when attempt to play, " + this.toString());
                sender.sendMessage("Something went wrong while playing the radio, the bot author has been notified. NO_MATCH");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sender.sendMessage("Failed to play `" + this.toString() + " Radio`, is it offline?");
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

    private void modifyTrackInfo(AudioTrackInfo info, String title, String artist) {
        try {
            if(titleField != null)
                titleField.set(info, title);
            if(artistField != null)
                artistField.set(info, artist);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Failed to modify radio track info.", e);
        }

    }

}
