package dev.armadeus.discord.audio.radio.stations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Request;

import java.awt.*;
import java.io.IOException;

public class Hive365 extends Radio {

    public Hive365() {
        super("hive365", "Hive Radio", "Hive365", "http://stream.hive365.co.uk:8088/live");
    }

    @Override
    public MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer issuer) {
        JsonObject streamData = getSongInfo();
        EmbedBuilder builder = EmbedUtils.newBuilder(issuer).setColor(Color.YELLOW)
                .setTitle("Radio provided by Hive365", "https://hive365.co.uk/")
                .setDescription("Non-stop music guaranteed to get ya buzzin'")
                .setThumbnail("https://hive365.co.uk/img/header-logo.png");

        if(streamData != null) {
            builder.setDescription(streamData.get("title").getAsString());
            builder.appendDescription("\nTitle: " + streamData.get("artist_song").getAsString());
        }
        return builder.build();
    }

    private JsonObject getSongInfo() {
        try {
            return JsonParser.parseString(CLIENT.newCall(new Request.Builder().url("https://hive365.co.uk/streaminfo/info.php").build()).execute().body().string()).getAsJsonObject().get("info").getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
