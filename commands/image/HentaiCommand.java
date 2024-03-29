package dev.armadeus.bot.commands.image;

import dev.armadeus.core.command.DiscordCommandSender;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.core.util.Utils;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.CommandTree;
import dev.armadeus.command.command.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.ImageBoard;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.kodehawa.lib.imageboards.entities.exceptions.QueryFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HentaiCommand extends CommandTree {

    private static final List<ImageBoard<? extends BoardImage>> boards;
    private static final Logger logger = LogManager.getLogger();

    static {
        boards = new ArrayList<>();
        boards.add(DefaultImageBoards.DANBOORU);
        boards.add(DefaultImageBoards.KONACHAN);
        boards.add(DefaultImageBoards.YANDERE);
    }

    private final Random RANDOM = new Random();

    private final AtomicInteger runs = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);

    public HentaiCommand() {
        getChildren().add(new SubCommand("hentai", "", "Gets a lewd image from hentai imageboards.") {
            @Override
            public void init() {
                super.setAliases(Arrays.asList("h"));
            }

            @Override
            public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
                if (sender instanceof DiscordCommandSender) {
                    DiscordCommandSender sendee = (DiscordCommandSender) sender;
                    GuildCommandSender gcs = sendee instanceof GuildCommandSender ? (GuildCommandSender) sendee : null;

                    if (gcs != null && !gcs.getTextChannel().isNSFW()) {
                        gcs.sendMessage(ChannelTarget.NSFW, "Sorry, " + gcs.getMember().getAsMention() + " but I can't satisfy that desire in here. :cry:");
                        return;
                    }

                    int page = RANDOM.nextInt(255);
                    runs.getAndIncrement();
                    ImageBoard<? extends BoardImage> board = boards.get(RANDOM.nextInt(boards.size()));
                    List<? extends BoardImage> images = null;
                    int attempts = 0;
                    while (true) {
                        if (attempts++ > 5) {
                            sendee.sendMessage("I couldn't find anything .-.");
                            return;
                        }
                        try {
                            images = board.get(page, 100).blocking();
                            if (images != null) {
                                images = images.stream().filter(image -> image.getRating().equals(Rating.EXPLICIT)
                                        && (image.getTags().stream().noneMatch(tag -> tag.equalsIgnoreCase("loli")))).collect(Collectors.toList());
                                if (!images.isEmpty())
                                    break;
                            }

                        } catch (QueryFailedException e) {
                            logger.debug("Failed to query " + board.getBoardType() + ", received a " + e.getCode() + ".");
                            return;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    BoardImage selection = images.get(RANDOM.nextInt(images.size()));
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setAuthor(board.getImageType().getSimpleName().replace("Image", "").replace("Furry", "E621") + ":", selection.getURL(), null);
                    //if (!selection.getTags().isEmpty())
                    //eb.setDescription("Tags: `" + selection.getTags().subList(0, Math.min(5, selection.getTags().size())) + "`");
                    //eb.setDescription(String.format("[%s](%s)", "link", selection.getImageUrl()));
                    eb.setFooter("Requested by: " + (gcs != null ? gcs.getMember().getEffectiveName() : sendee.getName()), sendee.getUser().getAvatarUrl());
                    eb.setColor(Utils.randomColor());
                    eb.setImage(selection.getURL());
                    if (selection.getURL().toLowerCase().contains("null")) {
                        // TODO: Query again? Send user a message?
                        sendee.sendMessage("Unfortunately I was unable to fetch you an image, please try again.");
                        logger.error(board.getBoardType() + " returned invalid URL!\tType: " + selection.getClass().getSimpleName() + "\tURL: " + selection.getURL() + "\tErrors: " + errors.incrementAndGet() + "/" + runs.get());
                        return;
                    }
                    sendee.sendMessage(ChannelTarget.NSFW, eb.build());
                }
            }
        });
    }

}
