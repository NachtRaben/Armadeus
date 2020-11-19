package com.nachtraben.armadeus.commands.image;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nachtraben.armadeus.Armadeus;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.TimedCache;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class NekoLifeCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(NekoLifeCommands.class);
    private static final String BASEURL = "http://nekos.life/api/neko";
    private static final String NSFWURL = "http://nekos.life/api/lewd/neko";

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
                    sendee.getTextChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:")).queue();
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
        Set<String> cache = gsendee != null ? guildSearchCache.computeIfAbsent(gsendee.getGuildId(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sender.getUserId(), set -> new HashSet<>());
        try {
            int tries = 0;
            while (url == null) {
                if (tries > 5)
                    break;
                JSONObject response;
                if (nsfw) {
                    response = Unirest.get(NSFWURL).asJson().getBody().getObject();
                } else {
                    response = Unirest.get(BASEURL).asJson().getBody().getObject();
                }
                if (response.has("neko")) {
                    url = response.getString("neko");
                    List<String> urls = (List<String>) Armadeus.getInstance().getConfig().getMetadata().computeIfAbsent("blacklisted-urls", k -> new ArrayList<String>());
                    if (urls.contains(url)) {
                        LOGGER.info("Stopping blacklisted URL: " + url);
                        url = null;
                    }
                    if (!cache.contains(url)) {
                        cache.add(url);
                        break;
                    }
                    tries++;
                }
            }
        } catch (UnirestException e) {
            LOGGER.warn("Failed to query catgirls api!", e);
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
