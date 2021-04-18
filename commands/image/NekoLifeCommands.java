package dev.armadeus.bot.commands.image;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Cmd;
import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.util.TimedCache;
import dev.armadeus.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NekoLifeCommands {
    private static final Logger logger = LogManager.getLogger();
    private static final String BASEURL = "http://nekos.life/api/neko";
    private static final String NSFWURL = "http://nekos.life/api/lewd/neko";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS).build();

    private final TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    private final TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);


    @Cmd(name = "neko", format = "", description = "Sends a cute cat for your viewing pleasure.")
    public void neko(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            sendNeko((DiscordCommandSender) sender, false);
        }
    }

    @Cmd(name = "nsfwneko", format = "", description = "Sends a lewd cat for your other pleasures.")
    public void nsfwNeko(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            if (sender instanceof GuildCommandSender) {
                GuildCommandSender sendee = (GuildCommandSender) sender;
                if (!sendee.getTextChannel().isNSFW()) {
                    sendee.getTextChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. :cry:").queue();
                    return;
                }
                sendNeko((DiscordCommandSender) sender, true);
            } else {
                sendNeko((DiscordCommandSender) sender, true);
            }
        }
    }

    private void sendNeko(DiscordCommandSender sender, boolean nsfw) {
        GuildCommandSender gsendee = sender instanceof GuildCommandSender ? (GuildCommandSender) sender : null;
        String url = null;
        Set<String> cache = gsendee != null ? guildSearchCache.computeIfAbsent(gsendee.getGuild().getIdLong(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sender.getUser().getIdLong(), set -> new HashSet<>());
        try {
            int tries = 0;
            while (url == null) {
                if (tries > 5)
                    break;
                JsonObject response;
                if (nsfw) {
                    response = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(client.newCall(new Request.Builder().url(NSFWURL).build()).execute().body()).byteStream())).getAsJsonObject();
                } else {
                    response = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(client.newCall(new Request.Builder().url(BASEURL).build()).execute().body()).byteStream())).getAsJsonObject();
                }
                if (response.has("neko")) {
                    url = response.get("neko").getAsString();
                    // TODO: Re-implement
//                    List<String> urls = (List<String>) BotConfig.get().computeIfAbsent("blacklisted-urls", k -> new ArrayList<String>());
//                    if (urls.contains(url)) {
//                        logger.info("Stopping blacklisted URL: " + url);
//                        url = null;
//                    }
                    if (!cache.contains(url)) {
                        cache.add(url);
                        break;
                    }
                    tries++;
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to query catgirls api!", e);
            sender.sendMessage("Sorry but I was unable to query the website.");
            return;
        }

        if (url == null) {
            sender.sendMessage("Sorry but I was unable to find a neko :c");
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setImage(url);
        eb.setColor(Utils.randomColor());
        if (gsendee != null)
            eb.setFooter("Requested by " + gsendee.getMember().getEffectiveName(), gsendee.getUser().getAvatarUrl());
        sender.sendMessage(eb.build());
    }

}
