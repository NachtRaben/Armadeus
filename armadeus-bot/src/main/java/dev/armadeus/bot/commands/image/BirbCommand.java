package dev.armadeus.bot.commands.image;

import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BirbCommand extends Command {

    private static final Logger logger = LogManager.getLogger();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS).build();
    
    private static final String IMGURL = "https://random.birb.pw/img/";
    private static final String NAMEURL = "http://random.birb.pw/tweet/";

    private final TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    private final TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);

    public BirbCommand() {
        super("birb", "", "Sends a birb");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            GuildCommandSender gsendee = sendee instanceof GuildCommandSender ? (GuildCommandSender) sendee : null;
            Set<String> cache = gsendee != null ? guildSearchCache.computeIfAbsent(gsendee.getGuild().getIdLong(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sendee.getUser().getIdLong(), set -> new HashSet<>());
            String url = null;
            try {
                int tries = 0;
                while (url == null) {
                    if (tries > 5)
                        break;
                    url = IMGURL + Objects.requireNonNull(client.newCall(new Request.Builder().url(NAMEURL).build()).execute().body()).string();
                    if (!cache.contains(url)) {
                        cache.add(url);
                    } else {
                        url = null;
                        tries++;
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to query birb api!", e);
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
