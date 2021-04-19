package dev.armadeus.bot.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    public static boolean tokenCompare(String trackTitle, String... keywords) {
        return tokenCompare(0.4f, trackTitle, keywords);
    }

    public static boolean tokenCompare(float target, String compareTo, String... keywords) {
        List<String> expected = Arrays.stream(keywords).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
        List<String> tokens = Arrays.stream(compareTo.toLowerCase().split(" ")).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
        int matches = 0;
        for (String token : tokens) {
            for (String exp : expected) {
                if (token.equals(exp))
                    matches++;
            }
        }
        return (float) matches >= (target * expected.size());
    }
}
