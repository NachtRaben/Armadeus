package com.nachtraben.core.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.Random;

public class Utils {

    private static final Random R = new Random();

    public static Color randomColor() {
        return new Color(R.nextInt(255), R.nextInt(255), R.nextInt(255));
    }

    public static int getEmbedLength(EmbedBuilder builder) {
        return builder.build().getLength();
    }

}
