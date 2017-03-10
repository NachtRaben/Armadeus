package com.nachtraben.core.audio;

import com.nachtraben.core.utils.LogManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;

/**
 * Created by NachtRaben on 2/21/2017.
 */
public class GuildMusicManager {

    private AudioPlayer player;
    private AudioEventAdapter listener;

    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        listener = new AudioEventAdapter(){};
        player.addListener(listener);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public AudioEventAdapter getScheduler() {
        return listener;
    }

    public void addEventListener(AudioEventAdapter eventAdapter) {
        player.removeListener(listener);
        this.listener = eventAdapter;
        player.addListener(listener);
    }

    public AudioPlayerSendHandler getSendHandler() {
        LogManager.ROOT.debug("getSendHandler() was called.");
        return new AudioPlayerSendHandler(player);
    }

}
