package com.nachtraben.tohsaka.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NachtRaben on 1/18/2017.
 */
public interface TrackMetaManager {

    Map<AudioTrack, ConcurrentHashMap<String, Object>> data = new ConcurrentHashMap<>();

    default Map<String, Object> getTrackMeta(AudioTrack key) {
        return data.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
    }

    default void deleteTrackMeta(AudioTrack key) {
        data.remove(key);
    }

}
