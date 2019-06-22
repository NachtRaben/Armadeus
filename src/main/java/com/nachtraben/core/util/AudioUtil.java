package com.nachtraben.core.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AudioUtil {

    private static final Logger log = LoggerFactory.getLogger(AudioUtil.class);

    private static Field titleField;
    private static Field artistField;

    // TODO: move this to utility
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
            log.error("Failed to modify AudioTrackInfo fields!", e);
        }
    }

    public static AudioTrackInfo transform(AudioTrackInfo info, String title, String artist) {
        try {
            if (titleField != null)
                titleField.set(info, title);
            if (artistField != null)
                artistField.set(info, artist);
            return info;
        } catch (IllegalAccessException e) {
            log.error("Failed to modify track info.", e);
        }
        return null;
    }

}
