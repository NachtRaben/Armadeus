package dev.armadeus.discord.audio.util;

import lavalink.client.player.track.AudioTrackInfo;
import lavalink.client.player.track.DefaultAudioTrackInfo;

import java.lang.reflect.Field;

public class AudioInfoModifier {

    private static Field titleField;
    private static Field artistField;
    private static Field identifierField;

    static {
        try {
            Class<?> clz = DefaultAudioTrackInfo.class;
            titleField = clz.getDeclaredField("title");
            titleField.setAccessible(true);
            artistField = clz.getDeclaredField("author");
            artistField.setAccessible(true);
            identifierField = clz.getDeclaredField("identifier");
            identifierField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private AudioTrackInfo info;

    public AudioInfoModifier(AudioTrackInfo info) {
        this.info = info;
    }

    public AudioInfoModifier setTitle(String title) {
        try {
            titleField.set(info, title);
        } catch (IllegalAccessException ignored) {}
        return this;
    }

    public AudioInfoModifier setIdentifier(String identifier) {
//        try {
//            identifierField.set(info, identifier);
//        } catch (IllegalAccessException ignored) {}
        return this;
    }

    public AudioInfoModifier setArtist(String artist) {
        try {
            artistField.set(info, artist);
        } catch (IllegalAccessException ignored){}
        return this;
    }

    public AudioTrackInfo getTrackInfo() {
        return info;
    }
}
