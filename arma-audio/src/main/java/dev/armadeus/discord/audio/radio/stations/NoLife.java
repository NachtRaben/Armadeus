package dev.armadeus.discord.audio.radio.stations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Request;

import java.awt.*;
import java.io.IOException;

public class NoLife extends Radio {

    public NoLife() {
        super("nolife", "NoLife Radio", "NoLife Radio", "http://listen.nolife-radio.com:8000/");
    }

    @Override
    public MessageEmbed getNowPlayingEmbed(Member issuer) {
        JsonObject streamData = getSongInfo();
        EmbedBuilder builder = EmbedUtils.newBuilder(issuer).setColor(Color.GREEN)
                .setTitle("Radio provided by NoLife Radio", "https://nolife-radio.com/")
                .setDescription("The Video Games Music Channel");

        if (streamData != null) {
            builder.appendDescription("\nTitle: " + streamData.get("title").getAsString());
        }
        return builder.build();
    }

    private JsonObject getSongInfo() {
        try {
            return JsonParser.parseString(CLIENT.newCall(new Request.Builder().url("http://listen.nolife-radio.com:8000/status-json.xsl").build()).execute().body().string()).getAsJsonObject().get("icestats").getAsJsonObject().get("source").getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
