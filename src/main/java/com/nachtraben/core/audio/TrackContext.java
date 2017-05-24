package com.nachtraben.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Created by NachtRaben on 3/17/2017.
 */
public class TrackContext {

    private AudioTrack track;
    private User requester;
    private TextChannel textChannel;

    public TrackContext(AudioTrack track, User user, TextChannel channel) {
        this.track = track;
        this.requester = user;
        this.textChannel = channel;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public User getRequester() {
        return requester;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public TrackContext clone() {
        return new TrackContext(track.makeClone(), requester, textChannel);
    }

}
