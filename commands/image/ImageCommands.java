package dev.armadeus.bot.commands.image;

import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.Utils;
import dev.armadeus.core.util.images.NMappedBoards;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.CommandTree;
import dev.armadeus.command.command.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kodehawa.lib.imageboards.ImageBoard;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.kodehawa.lib.imageboards.entities.exceptions.QueryFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImageCommands extends CommandTree {

    private static final Logger logger = LogManager.getLogger();
    private final Random RANDOM = new Random();

    private final AtomicInteger runs = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);

    public ImageCommands() {
        getChildren().add(new SubCommand("image", "", "Gets a lewd image from a random ImageBoard.") {
            @Override
            public void init() {
                super.setAliases(Arrays.asList("i"));
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

                    Rating rating = gcs != null && gcs.getTextChannel().isNSFW() ? Rating.EXPLICIT : Rating.SAFE;
                    if (explicit)
                        rating = Rating.EXPLICIT;
                    else if (questionable)
                        rating = Rating.QUESTIONABLE;
                    else if (safe)
                        rating = Rating.SAFE;

                    if (gcs != null && !gcs.getTextChannel().isNSFW() && !rating.equals(Rating.SAFE)) {
                        gcs.sendMessage(ChannelTarget.NSFW, "Sorry, " + gcs.getMember().getAsMention() + " but I can't satisfy that desire in here. :cry:");
                        return;
                    }

                    int page = RANDOM.nextInt(128);
                    runs.getAndIncrement();
                    ImageBoard<? extends BoardImage> board = rating.equals(Rating.SAFE) ? NMappedBoards.cleanBoards.get(RANDOM.nextInt(NMappedBoards.cleanBoards.size())) : NMappedBoards.nsfwBoards.get(RANDOM.nextInt(NMappedBoards.nsfwBoards.size()));
                    List<? extends BoardImage> images = null;
                    Rating finalRating = rating;
                    int attempts = 0;
                    while(true) {
                        if(attempts++ > 10) {
                            sendee.sendMessage("I couldn't find anything .-. wut.");
                            return;
                        }
                        try {
                            images = board.get(page, 100).blocking();
                            if(images != null) {
                                images = images.stream().filter(image -> image.getRating().equals(finalRating)
                                        && (image.getTags().stream().noneMatch(tag ->
                                        tag.equalsIgnoreCase("loli") ||
                                                tag.equalsIgnoreCase("shota") ||
                                                tag.equalsIgnoreCase("lolicon") ||
                                                tag.equalsIgnoreCase("shotacon")))).collect(Collectors.toList());
                                if(!images.isEmpty())
                                    break;
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ignored) {
                            }
                        } catch (QueryFailedException e) {
                            logger.debug("Failed to query " + board.getBoardType() + ", received a " + e.getCode() + ".");
                            return;
                        }
                    }
                    BoardImage selection = images.get(RANDOM.nextInt(images.size()));
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setAuthor(board.getImageType().getSimpleName().replace("Image", "").replace("Furry", "E621") + ":", selection.getURL(), null);
                    //if(!selection.getTags().isEmpty())
                        //eb.setDescription("Tags: `" + selection.getTags().subList(0, Math.min(5, selection.getTags().size())) + "`");                    eb.setFooter("Requested by: " + (gcs != null ? gcs.getMember().getEffectiveName() : sendee.getName()), sendee.getUser().getAvatarUrl());
                    eb.setColor(Utils.randomColor());
                    eb.setImage(selection.getURL());
                    if (selection.getURL().toLowerCase().contains("null")) {
                        // TODO: Query again? Send user a message?
                        sendee.sendMessage("Unfortunately I was unable to fetch you an image, please try again.");
                        logger.error(board.getBoardType() + " returned invalid URL!\tType: " + selection.getClass().getSimpleName() + "\tURL: " + selection.getURL() + "\tErrors: " + errors.incrementAndGet() + "/" + runs.get());
                        return;
                    }
                    if (finalRating.equals(Rating.SAFE))
                        sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
                    else
                        sendee.sendMessage(ChannelTarget.NSFW, eb.build());
                }
            }
        });
    }
}
