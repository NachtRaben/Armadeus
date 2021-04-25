//package com.nachtraben;
//
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//
//public class TimeTest {
//
//    private static final Logger logger = LogManager.getLogger();
//
//    public static void main(String[] args) {
//        String timeA = "1:50AM";
//        String timeB = "1:50PM";
//        String timeC = "10:50AM";
//        String timeD = "10:50PM";
//
//        String timeE = "00:30";
//        String timeF = "13:30";
//        String timeG = "0:30";
//
//        String timeH = "30m20s";
//        String timeI = "-30m20s";
//
//        String dateA = "07/08/17";
//        String dateB = "7/8/17";
//        String dateC = "07/08/2017";
//        String dateD = "7/8/2017";
//
//        logger.info("TIMES: " + LocalTime.now());
//        logger.info(timeA + ":\t" + DateTimeUtil.parseTime(timeA));
//        logger.info(timeB + ":\t" + DateTimeUtil.parseTime(timeB));
//        logger.info(timeC + ":\t" + DateTimeUtil.parseTime(timeC));
//        logger.info(timeD + ":\t" + DateTimeUtil.parseTime(timeD));
//        logger.info(timeE + ":\t" + DateTimeUtil.parseTime(timeE));
//        logger.info(timeF + ":\t" + DateTimeUtil.parseTime(timeF));
//        logger.info(timeG + ":\t" + DateTimeUtil.parseTime(timeG));
//        logger.info(timeH + ":\t" + DateTimeUtil.parseTime(timeH));
//        logger.info(timeI + ":\t" + DateTimeUtil.parseTime(timeI));
//
//
//        logger.info("");
//        logger.info("DATES: " + LocalDate.now());
//        logger.info(dateA + ":\t" + DateTimeUtil.parseDate(dateA));
//        logger.info(dateB + ":\t" + DateTimeUtil.parseDate(dateB));
//        logger.info(dateC + ":\t" + DateTimeUtil.parseDate(dateC));
//        logger.info(dateD + ":\t" + DateTimeUtil.parseDate(dateD));
//
//        logger.info("");
//        logger.info("DATE-TIME: " + LocalDateTime.now());
//        logger.info(dateA + "-" + timeA + ": " + DateTimeUtil.parseDateTime(dateA, timeA));
//        logger.info(dateA + "-" + timeB + ": " + DateTimeUtil.parseDateTime(dateA, timeB));
//        logger.info(dateA + "-" + timeC + ": " + DateTimeUtil.parseDateTime(dateA, timeC));
//        logger.info(dateA + "-" + timeD + ": " + DateTimeUtil.parseDateTime(dateA, timeD));
//        logger.info(dateA + "-" + timeE + ": " + DateTimeUtil.parseDateTime(dateA, timeE));
//        logger.info(dateA + "-" + timeF + ": " + DateTimeUtil.parseDateTime(dateA, timeF));
//        logger.info(dateA + "-" + timeG + ": " + DateTimeUtil.parseDateTime(dateA, timeG));
//        logger.info(dateA + "-" + timeH + ": " + DateTimeUtil.parseDateTime(dateA, timeH));
//        logger.info(dateA + "-" + timeI + ": " + DateTimeUtil.parseDateTime(dateA, timeI));
//        logger.info("");
//        logger.info(dateB + "-" + timeA + ": " + DateTimeUtil.parseDateTime(dateB, timeA));
//        logger.info(dateB + "-" + timeB + ": " + DateTimeUtil.parseDateTime(dateB, timeB));
//        logger.info(dateB + "-" + timeC + ": " + DateTimeUtil.parseDateTime(dateB, timeC));
//        logger.info(dateB + "-" + timeD + ": " + DateTimeUtil.parseDateTime(dateB, timeD));
//        logger.info(dateB + "-" + timeE + ": " + DateTimeUtil.parseDateTime(dateB, timeE));
//        logger.info(dateB + "-" + timeF + ": " + DateTimeUtil.parseDateTime(dateB, timeF));
//        logger.info(dateB + "-" + timeG + ": " + DateTimeUtil.parseDateTime(dateB, timeG));
//        logger.info(dateB + "-" + timeH + ": " + DateTimeUtil.parseDateTime(dateB, timeH));
//        logger.info(dateB + "-" + timeI + ": " + DateTimeUtil.parseDateTime(dateB, timeI));
//        logger.info("");
//        logger.info(dateC + "-" + timeA + ": " + DateTimeUtil.parseDateTime(dateC, timeA));
//        logger.info(dateC + "-" + timeB + ": " + DateTimeUtil.parseDateTime(dateC, timeB));
//        logger.info(dateC + "-" + timeC + ": " + DateTimeUtil.parseDateTime(dateC, timeC));
//        logger.info(dateC + "-" + timeD + ": " + DateTimeUtil.parseDateTime(dateC, timeD));
//        logger.info(dateC + "-" + timeE + ": " + DateTimeUtil.parseDateTime(dateC, timeE));
//        logger.info(dateC + "-" + timeF + ": " + DateTimeUtil.parseDateTime(dateC, timeF));
//        logger.info(dateC + "-" + timeG + ": " + DateTimeUtil.parseDateTime(dateC, timeG));
//        logger.info(dateC + "-" + timeH + ": " + DateTimeUtil.parseDateTime(dateC, timeH));
//        logger.info(dateC + "-" + timeI + ": " + DateTimeUtil.parseDateTime(dateC, timeI));
//        logger.info("");
//        logger.info(dateD + "-" + timeA + ": " + DateTimeUtil.parseDateTime(dateD, timeA));
//        logger.info(dateD + "-" + timeB + ": " + DateTimeUtil.parseDateTime(dateD, timeB));
//        logger.info(dateD + "-" + timeC + ": " + DateTimeUtil.parseDateTime(dateD, timeC));
//        logger.info(dateD + "-" + timeD + ": " + DateTimeUtil.parseDateTime(dateD, timeD));
//        logger.info(dateD + "-" + timeE + ": " + DateTimeUtil.parseDateTime(dateD, timeE));
//        logger.info(dateD + "-" + timeF + ": " + DateTimeUtil.parseDateTime(dateD, timeF));
//        logger.info(dateD + "-" + timeG + ": " + DateTimeUtil.parseDateTime(dateD, timeG));
//        logger.info(dateD + "-" + timeH + ": " + DateTimeUtil.parseDateTime(dateD, timeH));
//        logger.info(dateD + "-" + timeI + ": " + DateTimeUtil.parseDateTime(dateD, timeI));
//    }
//
//}
