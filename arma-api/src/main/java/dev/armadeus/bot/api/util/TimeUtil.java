package dev.armadeus.bot.api.util;

import lombok.Getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by NachtRaben on 1/22/2017.
 */
public class TimeUtil {

    private static final EnumSet<TimeUnit> TIME_UNITS = EnumSet.range(TimeUnit.YEAR, TimeUnit.SECOND);
    private static final long[] UNITS = TIME_UNITS.stream().mapToLong(t -> t.millis).toArray();
    private static final DateFormat SIMPLE_FORMAT = new SimpleDateFormat("MMM d, yyyy");
    private static final DateFormat FORMAT = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?:([0-9]+)\\s*ye?a?r?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*mon?t?h?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*we?e?k?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*da?y?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*ho?u?r?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:mill?i?s?e?c?o?n?d?s?|ms)[,\\s]*)?" +
                    "(?:([0-9]+)\\s*mi?n?u?t?e?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:se?c?o?n?d?s?)?)?",
            Pattern.CASE_INSENSITIVE
    );

    public static long parse(String timeFormat) throws NumberFormatException {
        long total = 0;
        boolean found = false;
        Matcher matcher = TIME_PATTERN.matcher(timeFormat);
        while (matcher.find()) {
            String main = matcher.group();
            if (main == null || main.isEmpty()) {
                continue;
            }
            for (int i = 0; i <= UNITS.length; i++) {
                String group = matcher.group(i + 1);
                if (group == null || group.isEmpty()) {
                    continue;
                }
                found = true;
                if (i < 5) {
                    total += Integer.parseInt(group) * UNITS[i];
                } else if (i == 5) {
                    total += Long.parseLong(group);
                } else {
                    total += Integer.parseInt(group) * UNITS[i - 1];
                }
            }
        }
        if (!found && !timeFormat.isEmpty()) {
            throw new NumberFormatException(timeFormat);
        }
        return total;
    }

    public static String formatDate(long date) {
        return FORMAT.format(date);
    }

    public static String formatDate(long date, boolean simple) {
        return simple ? SIMPLE_FORMAT.format(date) : FORMAT.format(date);
    }

    public static String format(long time) {
        return formatDifference(0, time, true, TIME_UNITS);
    }

    public static String format(long time, boolean abbreviate) {
        return formatDifference(0, time, abbreviate, TIME_UNITS);
    }

    public static String format(long time, TimeUnit unit, TimeUnit... units) {
        return formatDifference(0, time, true, EnumSet.of(unit, units));
    }

    public static String format(long time, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(0, time, abbreviate, EnumSet.of(unit, units));
    }

    public static String formatFromNow(long to) {
        return formatDifference(System.currentTimeMillis(), to, true, TIME_UNITS);
    }

    public static String formatFromNow(long to, boolean abbreviate) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, TIME_UNITS);
    }

    public static String formatFromNow(long to, TimeUnit unit, TimeUnit... units) {
        return formatDifference(System.currentTimeMillis(), to, true, EnumSet.of(unit, units));
    }

    public static String formatFromNow(long to, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, EnumSet.of(unit, units));
    }

    public static String formatDifference(long from, long to) {
        return formatDifference(from, to, true, TIME_UNITS);
    }

    public static String formatDifference(long from, long to, boolean abbreviate) {
        return formatDifference(from, to, abbreviate, TIME_UNITS);
    }

    public static String formatDifference(long from, long to, TimeUnit unit, TimeUnit... units) {
        return formatDifference(from, to, true, EnumSet.of(unit, units));
    }

    public static String formatDifference(long from, long to, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(from, to, abbreviate, EnumSet.of(unit, units));
    }

    public static String formatDifference(long from, long to, boolean abbreviate, EnumSet<TimeUnit> units) throws IllegalArgumentException {

        checkArgument(!units.isEmpty(), "requires at least one TimeUnit");
        // Ensure from is always <= to
        if (from > to) {
            long temp = from;
            from = to;
            to = temp;
        }

        TimeUnit lowest = null;
        long difference = to - from;
        StringBuilder sb = new StringBuilder(units.size() * (abbreviate ? 15 : 10)); // Estimate size
        for (TimeUnit unit : units) {

            lowest = unit; // Will always be the last iteration
            long ms = unit.millis;
            long found = difference / ms;
            if (found > 0) {
                difference -= found * ms;
                sb.append(found).append(unit.getSuffix(abbreviate, found == 1)).append(' ');
            }
        }

        if (difference == to - from) { // Nothing was formatted
            return sb.append('0').append(lowest.getSuffix(abbreviate, false)).toString();
        }

        // Cut off the extra space at the end
        return sb.substring(0, sb.length() - 1);
    }

    @Getter
    public enum TimeUnit {
        YEAR(1000L * 60 * 60 * 24 * 365, " year", "y"),
        MONTH(1000L * 60 * 60 * 24 * 30, " month", "mo"),
        WEEK(1000L * 60 * 60 * 24 * 7, " week", "w"),
        DAY(1000L * 60 * 60 * 24, " day", "d"),
        HOUR(1000L * 60 * 60, " hour", "h"),
        MINUTE(1000L * 60, " minute", "m"),
        SECOND(1000L, " second", "s"),
        MILLISECOND(1, " millisecond", "ms");

        private final long millis;
        private final String singular, plural, abbreviated;

        TimeUnit(long millis, String singular, String abbreviated) {
            this.millis = millis;
            this.singular = singular;
            this.plural = singular + 's'; // All just add an s
            this.abbreviated = abbreviated;
        }

        public String getSuffix(boolean abbreviate, boolean singular) {
            return abbreviate ? this.abbreviated : singular ? this.singular : this.plural;
        }
    }
}
