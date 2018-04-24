package com.nachtraben.tohsaka.util;

import com.nachtraben.orangeslice.CommandSender;
import net.kodehawa.lib.imageboards.ImageBoard;

import java.util.Random;

public class ImageUtils {

    private static final Random rand = new Random();

    public static void getImage(ImageBoard<?> api, ImageRequestType type, CommandSender sender, String imageboard, String[] flags) {

    }

    public enum ImageRequestType {
        GET, RANDOM, TAGS
    }

}
