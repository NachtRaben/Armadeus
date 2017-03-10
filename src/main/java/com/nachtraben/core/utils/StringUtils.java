package com.nachtraben.core.utils;
// Created by Xilixir on 2017-02-06

import java.util.ArrayList;

public class StringUtils {

    public static String arrayToString(String[] args) {
        if (args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        return sb.toString();
    }

    public static String removeFirstChars(String s, char c) {
        String str = "";
        boolean p = false;
        for (char c2 : s.toCharArray()) {
            if (c2 != c || p) {
                p = true;
                str += c2;
            }
        }
        return str;
    }

    public static String format(String content, Object... args) {
        StringBuilder out = new StringBuilder();
        int index = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '%' && (i + 1) < content.length() && content.charAt(i + 1) == 's') {
                if (index >= args.length) {
                    out.append("[MISSING ARGUMENT]");
                    continue;
                }
                out.append(args[index]);
                index++;
                i++;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String[] splitAtFirst(String s, char c) {
        String s1 = "";
        String s2 = "";
        boolean b = false;
        for (char c1 : s.toCharArray()) {
            if (c1 == c) {
                b = true;
            } else if (b) {
                s2 += c1;
            } else {
                s1 += c1;
            }
        }
        return new String[]{s1, s2};
    }

    public static String[] splitAt(String s, char c2) {
        ArrayList<String> a = new ArrayList<>();
        String s2 = "";
        for (char c : s.toCharArray()) {
            if (c == c2) {
                if (s2.length() > 0) {
                    a.add(s2);
                    s2 = "";
                }
            } else {
                s2 += c;
            }
        }
        if (s2.length() > 0) {
            a.add(s2);
        }
        return a.toArray(new String[a.size()]);
    }

    public static String[] splitAtSpaces(String s) {
        return splitAt(s, ' ');
    }
}
