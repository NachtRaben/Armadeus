package com.nachtraben.tohsaka.commands;

import com.google.common.cache.*;
import com.google.common.collect.MapMaker;
import com.nachtraben.TimedCache;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.kodehawa.lib.imageboards.entities.YandereImage;
import net.kodehawa.lib.imageboards.util.Imageboards;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class YandereCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(YandereCommand.class);
    private TimedCache<Long, Set<String>> cache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);

    private final Random r = new Random();

    public YandereCommand() {
        super("yandere", "(tags)", "Gets an image from Yandere");
        super.setAliases(Arrays.asList("yande.re", "yand", "yd"));
        super.setFlags(Arrays.asList("--nsfw"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;

            boolean isNSFW = flags.containsKey("nsfw");

            if (isNSFW && !sendee.getTextChannel().isNSFW()) {
                sendee.getTextChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:")).queue();
                return;
            }

            String search = args.get("tags");
            List<YandereImage> images = search != null ? Imageboards.YANDERE.onSearchBlocking(100, search) : Imageboards.YANDERE.getBlocking(100);
            images = images.stream().filter(data -> {
                if (isNSFW)
                    return data.getRating().toLowerCase().matches("[e|q]");
                else
                    return data.getRating().equalsIgnoreCase("s");
            }).collect(Collectors.toList());
            Set<String> cached = cache.computeIfAbsent(sendee.getGuild().getIdLong(), set -> new HashSet<>());
            System.out.println("Found: " + images.size() + "\tCached: " + cached.size());
            images = images.stream().filter(image -> !cached.contains(image.getJpeg_url())).collect(Collectors.toList());
            System.out.println("Filtered: " + images.size());
            if (images.isEmpty()) {
                if (isNSFW)
                    sendee.sendMessage(ChannelTarget.NSFW, "Sorry, no images were found, try again later.");
                else
                    sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, no images were found, try again later.");
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Utils.randomColor());
            YandereImage selection = images.get(r.nextInt(images.size()));
            eb.setImage(selection.getJpeg_url());
            eb.setTitle(search == null ? "Yandere" : search, selection.getJpeg_url());
            if (isNSFW)
                sendee.sendMessage(ChannelTarget.NSFW, eb.build());
            else
                sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
            cached.add(selection.getJpeg_url());
        }
    }
}
