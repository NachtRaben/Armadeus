package com.nachtraben.tohsaka.commands.image;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.TimedCache;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BirbCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirbCommand.class);

    private static final String IMGURL = "https://random.birb.pw/img/";
    private static final String NAMEURL = "http://random.birb.pw/tweet/";

    private TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    private TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);

    public BirbCommand() {
        super("birb", "", "Sends a birb");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            GuildCommandSender gsendee = sendee instanceof GuildCommandSender ? (GuildCommandSender) sendee : null;
            Set<String> cache = gsendee != null ? guildSearchCache.computeIfAbsent(gsendee.getGuildId(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sendee.getUserID(), set -> new HashSet<>());
            String url = null;
            try {
                int tries = 0;
                while (url == null) {
                    if (tries > 5)
                        break;
                    url = IMGURL + Unirest.get(NAMEURL).asString().getBody();
                    if (!cache.contains(url)) {
                        cache.add(url);
                    } else {
                        url = null;
                        tries++;
                    }
                }
            } catch (UnirestException e) {
                LOGGER.warn("Failed to query birb api!", e);
                sender.sendMessage("Sorry but I was unable to query the website.");
                return;
            }

            if (url == null) {
                sendee.sendMessage("Sorry but I was unable to find a birb :c");
                return;
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage(url);
            eb.setColor(Utils.randomColor());
            if (gsendee != null)
                eb.setFooter("Requested by " + gsendee.getMember().getEffectiveName(), sendee.getUser().getAvatarUrl());
            sendee.sendMessage(eb.build());

        }
    }
}
