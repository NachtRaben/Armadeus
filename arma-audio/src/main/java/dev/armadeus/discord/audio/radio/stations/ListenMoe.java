package dev.armadeus.discord.audio.radio.stations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ListenMoe extends Radio {

    private static MoeTrack current;
    private static OkHttpClient client = new OkHttpClient();

    public ListenMoe() {
        super("moe", "Moe Radio", "LISTEN.moe", "https://listen.moe/stream");
        connect();
    }

    void connect() {
        Request request = new Request.Builder().url("wss://listen.moe/gateway_v2").build();
        client.newWebSocket(request, new MoeListener());
    }

    @Override
    public MessageEmbed getNowPlayingEmbed(DiscordCommandIssuer issuer) {
        EmbedBuilder builder = EmbedUtils.newBuilder(issuer).setColor(Color.PINK)
                .setTitle("Radio provided by LISTEN.moe", "https://listen.moe/")
                .setDescription("JPop Radio Station");

        if(current != null) {
            builder.appendDescription("\nTitle: " + current.title);
            builder.appendDescription("\nArtist: " + Arrays.stream(current.artists).map(art -> art.name).collect(Collectors.joining(", ")));
            if(current.albums != null && current.albums.length > 0) {
                builder.setThumbnail(current.albums[0].getCoverURL());
                builder.appendDescription("\nAlbum: " + current.albums[0].name);
            }
        }
        return builder.build();
    }

    class MoeListener extends WebSocketListener {

        private final Gson gson = new GsonBuilder().create();

        private ScheduledTask task;

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            MoeMessage msg = gson.fromJson(text, MoeMessage.class);
            switch (msg.op) {
                case 0:
                    long heartbeat = msg.d.get("heartbeat").getAsLong();
                    task = ArmaAudio.core().scheduler().buildTask(ArmaAudio.get(), () -> webSocket.send("{ \"op\": 9 }")).repeat(heartbeat, TimeUnit.MILLISECONDS).schedule();
                    break;
                case 1:
                    MoeUpdate update = gson.fromJson(msg.d, MoeUpdate.class);
                    ListenMoe.current = update.song;
                    break;
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            ListenMoe.this.log.warn("Disconnected from moe - {}, {}", code, reason);
            reconnect();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            try {
                ListenMoe.this.log.warn("Disconnected from moe - {}", response.body().string());
            } catch (Exception ignored) {
                ListenMoe.this.log.error("Error connecting to moe", t);
            }
            reconnect();
        }

        private void reconnect() {
            ListenMoe.this.log.warn("Disconnected from moe, reconnecting");
            if(task != null) {
                task.cancel();
            }
            ArmaAudio.core().scheduler().buildTask(ArmaAudio.get(), ListenMoe.this::connect).delay(5, TimeUnit.SECONDS).schedule();
        }
    }

    static class MoeMessage {
        int op;
        JsonObject d;
    }

    static class MoeUpdate {
        MoeTrack song;
        MoeTrack[] lastPlayed;
    }

    static class MoeTrack {
        long id;
        String title;
        MoeArtist[] artists;
        MoeAlbum[] albums;
        long duration;
    }

    static class MoeArtist {
        long id;
        String name;
        String nameRomanji;
        String image;

        public String getArtistURL() {
            return "https://cdn.listen.moe/artists/" + image;
        }
    }

    static class MoeAlbum {
        long id;
        String name;
        String nameRomanji;
        String image;

        public String getCoverURL() {
            return "https://cdn.listen.moe/covers/" + image;
        }
    }
}
