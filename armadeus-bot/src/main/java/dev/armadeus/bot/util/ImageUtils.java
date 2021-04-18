package dev.armadeus.bot.util;

//import dev.armadeus.command.CommandSender;
import dev.armadeus.core.command.DiscordUser;
import net.kodehawa.lib.imageboards.ImageBoard;

import java.util.Random;

public class ImageUtils {

    private static final Random rand = new Random();

    public static void getImage(ImageBoard<?> api, ImageRequestType type, DiscordUser sender, String imageboard, String[] flags) {

    }

    public enum ImageRequestType {
        GET, RANDOM, TAGS
    }

}
