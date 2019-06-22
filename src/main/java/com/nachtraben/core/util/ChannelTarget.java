package com.nachtraben.core.util;

public enum ChannelTarget {

    GENERIC,
    MUSIC,
    NSFW,
    BOT_ANNOUNCEMENT,
    TWITCH_ANNOUNCEMENT;

    public static ChannelTarget forName(String name) {
        for(ChannelTarget ct : values()) {
            if(name.equalsIgnoreCase(ct.toString()))
                return ct;
        }
        return null;
    }

}
