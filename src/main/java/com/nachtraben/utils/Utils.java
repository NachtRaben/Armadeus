package com.nachtraben.utils;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public class Utils {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    private static final int WEEK = 7 * DAY;

    public static String colorString(String s) {
        return s.replace("#R#", Reference.ANSI_RESET)
                .replace("#bl#", Reference.ANSI_BLACK)
                .replace("#r#", Reference.ANSI_RED)
                .replace("#g#", Reference.ANSI_GREEN)
                .replace("#y#", Reference.ANSI_YELLOW)
                .replace("#b#", Reference.ANSI_BLUE)
                .replace("#c#", Reference.ANSI_CYAN)
                .replace("#w#", Reference.ANSI_WHITE) + Reference.ANSI_RESET;
    }

    public static String format(String content, Object... args){
        String out = "";
        int index = 0;
        for (int i = 0; i < content.length(); i++){
            char c = content.charAt(i);
            if (c == '%' && (i + 1) < content.length() && content.charAt(i+1) == 's'){
                if(index > args.length) throw new IllegalArgumentException("Attempted to format a string with an invalid number of arguments!");
                out += args[index];
                index++;
                i++;
            } else {
                out += c;
            }
        }
        return out;
    }

}
