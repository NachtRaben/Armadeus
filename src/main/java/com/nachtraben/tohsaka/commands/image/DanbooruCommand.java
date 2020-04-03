package com.nachtraben.tohsaka.commands.image;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.TimedCache;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.kodehawa.lib.imageboards.entities.impl.DanbooruImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DanbooruCommand extends Command {

    private static final Random RAND = new Random();
    private static final Logger log = LoggerFactory.getLogger(DanbooruCommand.class);
    private final TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    private final TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);


    private final Random r = new Random();

    public DanbooruCommand() {
        super("danbooru", "(tag)", "Gets an image from Rule34.");
        super.setAliases(Arrays.asList("danboo", "danb"));
        super.setFlags(Arrays.asList("--safe", "--questionable", "--explicit", "-seq"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            GuildCommandSender gcs = sendee instanceof GuildCommandSender ? (GuildCommandSender) sendee : null;

            boolean safe = flags.containsKey("safe") || flags.containsKey("s");
            boolean questionable = flags.containsKey("questionable") || flags.containsKey("q");
            boolean explicit = flags.containsKey("explicit") || flags.containsKey("e");
            boolean isSearch = args.containsKey("tag");

            Rating rating = gcs != null && gcs.getTextChannel().isNSFW() ? Rating.EXPLICIT : Rating.SAFE;
            if (explicit)
                rating = Rating.EXPLICIT;
            else if (questionable)
                rating = Rating.QUESTIONABLE;
            else if (safe)
                rating = Rating.SAFE;

            if (gcs != null && !gcs.getTextChannel().isNSFW() && !rating.equals(Rating.SAFE)) {
                gcs.sendMessage(ChannelTarget.GENERIC, "Sorry, " + gcs.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:"));
                return;
            }

            Rating finalRating = rating;
            List<DanbooruImage> images = isSearch ? DefaultImageBoards.DANBOORU.search(100, args.get("tag").replace(" ", "_")).blocking() : DefaultImageBoards.DANBOORU.get(RAND.nextInt(1024)).blocking();
            if (images != null) {
                Set<String> cache = gcs != null ? guildSearchCache.computeIfAbsent(gcs.getGuildId(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sendee.getUserId(), set -> new HashSet<>());
                images = images.stream().filter(image -> finalRating.equals(image.getRating()) && (image.getTags().stream().noneMatch(tag ->
                        tag.equalsIgnoreCase("loli") ||
                                tag.equalsIgnoreCase("shota") ||
                                tag.equalsIgnoreCase("lolicon") ||
                                tag.equalsIgnoreCase("shotacon")))).filter(image -> !cache.contains(image.getURL())).collect(Collectors.toList());
                if (images.isEmpty()) {
                    if (isSearch) {
                        if (!rating.equals(Rating.SAFE))
                            sendee.sendMessage(ChannelTarget.NSFW, "Sorry, but I didn't find anything for the tag, `" + args.get("tag") + "`.");
                        else
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I didn't find anything for the tag, `" + args.get("tag") + "`.");
                    } else {
                        if (!rating.equals(Rating.SAFE))
                            sendee.sendMessage(ChannelTarget.NSFW, "Sorry, but I wasn't able to find an image.");
                        else
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I wasn't able to find an image.");
                    }
                    return;
                }
                DanbooruImage selection = images.get(RAND.nextInt(images.size()));
                cache.add(selection.getURL());
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Utils.randomColor());
                eb.setAuthor(isSearch ? args.get("tag") : "Danbooru", selection.getURL(), null);
//                eb.setDescription("[link](" + selection.getImageUrl() + ")");
                eb.setImage(selection.getURL());
                eb.setFooter("Requested by: " + (gcs != null ? gcs.getMember().getEffectiveName() : sendee.getName()), sendee.getUser().getAvatarUrl());
                if (finalRating.equals(Rating.SAFE))
                    sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
                else
                    sendee.sendMessage(ChannelTarget.NSFW, eb.build());
            } else {
                // TODO: Query error, could be invalid page, could be an actual outage. Re-Query?
                sendee.sendMessage("Sorry, but I was unable to query the Yande.re, try again later.");
            }
        }
    }
}
