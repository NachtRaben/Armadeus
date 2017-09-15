package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.core.util.images.NMappedBoards;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.command.SubCommand;
import com.nachtraben.tohsaka.ImageRating;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.kodehawa.lib.imageboards.ImageboardAPI;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.QueryFailedException;
import net.kodehawa.lib.imageboards.util.Imageboards;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HentaiCommand extends CommandTree {

    private static List<ImageboardAPI<? extends BoardImage>> boards;
    private static final Logger LOGGER = LoggerFactory.getLogger(HentaiCommand.class);

    static {
        boards = new ArrayList<>();
        boards.add(Imageboards.DANBOORU);
        boards.add(Imageboards.KONACHAN);
        boards.add(Imageboards.YANDERE);
    }

    private final Random RANDOM = new Random();

    private AtomicInteger runs = new AtomicInteger(0);
    private AtomicInteger errors = new AtomicInteger(0);

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


                    final ImageRating rating = ImageRating.EXPLICIT;

                    if (gcs != null && !gcs.getTextChannel().isNSFW()) {
                        gcs.sendMessage(ChannelTarget.NSFW, "Sorry, " + gcs.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:"));
                        return;
                    }

                    int page = RANDOM.nextInt(255);
                    runs.getAndIncrement();
                    ImageboardAPI<? extends BoardImage> board = boards.get(RANDOM.nextInt(boards.size()));
                    List<? extends BoardImage> images = null;
                    int attempts = 0;
                    while (images == null || images.isEmpty()) {
                        if (attempts++ > 5) {
                            sendee.sendMessage("I couldn't find anything .-.");
                            return;
                        }
                        try {
                            images = board.getBlocking(page, 100);
                            if (images != null) {
                                images = images.stream().filter(image -> rating.matches(image.getRating().toLowerCase())
                                        && (image.getTags().stream().noneMatch(tag -> tag.equalsIgnoreCase("loli")))).collect(Collectors.toList());
                                if(!images.isEmpty())
                                    break;
                            }

                            } catch(QueryFailedException e){
                                LOGGER.debug("Failed to query " + board.getBoardType().name() + ", received a " + e.getCode() + ".");
                                return;
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        BoardImage selection = images.get(RANDOM.nextInt(images.size()));
                        LOGGER.debug(selection.getImageUrl());
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setAuthor(board.getImageType().getSimpleName().replace("Image", "").replace("Furry", "E621") + ":", selection.getImageUrl(), null);
                        //if (!selection.getTags().isEmpty())
                        //eb.setDescription("Tags: `" + selection.getTags().subList(0, Math.min(5, selection.getTags().size())) + "`");
                        //eb.setDescription(String.format("[%s](%s)", "link", selection.getImageUrl()));
                        eb.setFooter("Requested by: " + (gcs != null ? gcs.getMember().getEffectiveName() : sendee.getName()), sendee.getUser().getAvatarUrl());
                        eb.setColor(Utils.randomColor());
                        eb.setImage(selection.getImageUrl());
                        if (selection.getImageUrl().toLowerCase().contains("null")) {
                            // TODO: Query again? Send user a message?
                            sendee.sendMessage("Unfortunately I was unable to fetch you an image, please try again.");
                            LOGGER.error(board.getBoardType().name() + " returned invalid URL!\tType: " + selection.getClass().getSimpleName() + "\tURL: " + selection.getImageUrl() + "\tErrors: " + errors.incrementAndGet() + "/" + runs.get());
                            return;
                        }
                        if (rating.equals(ImageRating.SAFE))
                            sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
                        else
                            sendee.sendMessage(ChannelTarget.NSFW, eb.build());
                    }
                }
            });
        }

    }
