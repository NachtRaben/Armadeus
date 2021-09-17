package dev.armadeus.discord.audio.radio.stations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ListenMoe extends Radio {

    public ListenMoe() {
        super("moe", "Moe Radio", "LISTEN.moe", "https://listen.moe/stream");
    }
//TODO: Implement WebSocket Fetching of Song MetaData
    @Override
    public MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer issuer) {
        //JsonObject streamData = getSongInfo();
        EmbedBuilder builder = EmbedUtils.newBuilder(issuer).setColor(Color.WHITE)
                .setTitle("Radio provided by LISTEN.moe", "https://listen.moe/")
                .setDescription("JPop Radio Station");

      /*  if(streamData != null) {
            builder.appendDescription("\nTitle: " + streamData.get("title").getAsString());
        }*/
        return builder.build();
    }
 /*   private JsonObject getSongInfo() {
        try {
            return JsonParser.parseString(CLIENT.newCall(new Request.Builder().url("http://listen.nolife-radio.com:8000/status-json.xsl").build()).execute().body().string()).getAsJsonObject().get("icestats").getAsJsonObject().get("source").getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
