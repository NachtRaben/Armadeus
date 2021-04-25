package dev.armadeus.bot.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    public static boolean tokenCompare(String trackTitle, String... keywords) {
        return tokenCompare(0.4f, trackTitle, keywords);
    }

    public static boolean tokenCompare(float target, String compareTo, String... keywords) {
        List<String> expected = Arrays.stream(String.join(" ", keywords).split(" "))
                .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z\\d\\s]", "").trim())
                .filter(s2 -> !s2.isEmpty())
                .collect(Collectors.toList());
        List<String> tokens = Arrays.stream(compareTo.split(" "))
                .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z\\d\\s]", "").trim())
                .filter(s2 -> !s2.isEmpty())
                .collect(Collectors.toList());
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
