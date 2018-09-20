package com.nachtraben.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioPlayerSendHandler implements AudioSendHandler {

    public static boolean DEBUG = false;
    private static final Logger log = LoggerFactory.getLogger(AudioSendHandler.class);

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        byte[] data = lastFrame != null ? lastFrame.getData() : null;
        lastFrame = null;
        return data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
