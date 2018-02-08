package com.nachtraben.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtil {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    private static final DateTimeFormatter DATE_TIME_ZONE = DateTimeFormatter.ofPattern("MM/dd/yy-h:mma z", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("M/d/yy-h:mma", Locale.ENGLISH);

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_24H = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

    private static final DateTimeFormatter DATE_SHORT_YEAR = DateTimeFormatter.ofPattern("M/d/yy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_LONG_YEAR = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);

    private static final Format format = new SimpleDateFormat("HH:mm:ss");


    public static LocalTime parseTime(String time) {
        DateTimeFormatter selected = time.toUpperCase().matches(".*[AM|PM]") ? TIME : TIME_24H;
        try {
            return LocalTime.parse(time.toUpperCase(), selected);
        } catch (DateTimeParseException e) {
            long millis = TimeUtil.stringToMillis(time);
            if (millis != 0) {
                return LocalTime.now().plusSeconds(TimeUnit.MILLISECONDS.toSeconds(millis));
            } else {
                log.error("Failed to parse " + time + " to LocalTime.", e);
            }
        }
        return null;
    }

    public static LocalDate parseDate(String date) {
        DateTimeFormatter selected = date.lastIndexOf("/") == (date.length() - 5) ? DATE_LONG_YEAR : DATE_SHORT_YEAR;
        try {
            return LocalDate.parse(date, selected);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse " + date + " to LocalDate", e);
        }
        return null;
    }

    public static LocalDateTime parseDateTime(String date, String time) {
        LocalDate parsedDate = parseDate(date);
        LocalTime parsedTime = parseTime(time);
        if (parsedDate != null && parsedTime != null)
            return LocalDateTime.of(parsedDate, parsedTime);
        return null;
    }

    public static String formatTime(long l) {
        Date date = new Date(l);
        return format.format(date);
    }

}
