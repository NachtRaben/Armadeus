package com.nachtraben.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioPlayerSendHandler implements AudioSendHandler {

    private static final Logger log = LoggerFactory.getLogger(AudioSendHandler.class);

    public static boolean DEBUG = false;
    private final AudioPlayer player;
    private AudioFrame lastFrame;

    public AudioPlayerSendHandler(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public boolean canProvide() {
        if (DEBUG)
            log.debug("Debugging.");
        getFrame();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        if (DEBUG)
            log.debug("Providing");
        getFrame();
        byte[] data = lastFrame != null ? lastFrame.data : null;
        lastFrame = null;
        return data;
    }

    private AudioFrame getFrame() {
        if (lastFrame == null) {
            if (DEBUG)
                log.debug("Calling player.provide()");
            lastFrame = player.provide();
            if (DEBUG)
                log.debug(String.valueOf(lastFrame.data));
        }
        return lastFrame;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
