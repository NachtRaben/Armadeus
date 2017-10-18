package com.nachtraben.core.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by NachtRaben on 1/22/2017.
 */
public class TimeUtil {

    private static final long MILLISECOND = 1;
    private static final long SECOND = 1000 * MILLISECOND;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;

    public static long stringToMillis(String s) {
        boolean neg = s.charAt(0) == '-';
        if(neg) s = s.substring(1);
        String timePattern = "[a-zA-Z]"; // Catches 0-9
        String scalePattern = "[0-9]"; // Catches A-Z
        String[] values = s.split(timePattern);
        String[] scales = s.split(scalePattern);
        scales = Arrays.stream(scales).filter(s2 -> (s2 != null && s2.length() > 0)).toArray(String[]::new);
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        if (scales.length >= 1) {
            for (int i = 0; i < scales.length; i++) {
                String temp = scales[i].toLowerCase();
                if(temp.equals("w")) {
                    weeks = Integer.parseInt(values[i]);
                }
                else if (temp.equals("d")) {
                    days = Integer.parseInt(values[i]);
                }
                else if (temp.equals("h")){
                    hours = Integer.parseInt(values[i]);
                }
                else if (temp.equals("m")) {
                    minutes = Integer.parseInt(values[i]);
                }
                else if (temp.equals("s")) {
                    seconds = Integer.parseInt(values[i]);
                }
            }
        }
        return (neg ? -1 : 1) * (TimeUnit.DAYS.toMillis(weeks * 7) + TimeUnit.DAYS.toMillis(days) + TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds));
    }

    public static String fromLong(Long t, FormatType format) {
        boolean neg = t < 0;
        if (neg) t = t * -1;
        int size = t >= WEEK ? 5 : (t >= DAY ? 4 : (t >= HOUR ? 3 : (t >= MINUTE ? 2 : 1)));
        long weeks = 0;
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        switch (size) {
            case (5):
                weeks = t / WEEK;
            case (4):
                days = (t %= WEEK) / DAY;
            case (3):
                hours = (t %= DAY) / HOUR;
            case (2):
                minutes = (t %= HOUR) / MINUTE;
            case (1):
                seconds = t % MINUTE / SECOND;
        }

        if (format.equals(FormatType.TIME)) {
            switch (size) {
                case (5):
                    return String.format("%s%d:%02d:%02d:%02d:%02d", neg ? "-" : "", weeks, days, hours, minutes, seconds);
                case (4):
                    return String.format("%s%d:%02d:%02d:%02d", neg ? "-" : "", days, hours, minutes, seconds);
                case (3):
                    return String.format("%s%d:%02d:%02d", neg ? "-" : "", hours, minutes, seconds);
                case (2):
                    return String.format("%s%d:%02d", neg ? "-" : "", minutes, seconds);
                case (1):
                    return String.format("%s%d:%02d", neg ? "-" : "", 0, seconds);
                default:
                    return "ERROR";
            }
        } else if (format.equals(FormatType.STRING)) {
            StringBuilder sb = new StringBuilder();
            if(neg) sb.append("-");
            if (weeks > 0) sb.append(weeks).append("w");
            if (days > 0) sb.append(days).append("d");
            if (hours > 0) sb.append(hours).append("h");
            if (minutes > 0) sb.append(minutes).append("m");
            if (seconds >= 0) sb.append(seconds).append("s");
            return sb.toString();
        }
        return null;
    }

    public enum FormatType {
        STRING,
        TIME
    }

}
