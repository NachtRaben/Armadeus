package com.nachtraben.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class HasteBin {
    public static String postHaste(String content) {
        try {
            URL url = new URL("https://hastebin.com/documents");
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.addRequestProperty("user-agent", "DiscordBot v1.0");
            connection.addRequestProperty("content-length", content.length() + "");
            connection.addRequestProperty("content-type", "application/json; charset=UTF-8");
            connection.connect();

            OutputStream os = connection.getOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
            pw.write(content);
            pw.close();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            String str = sb.toString();
            return "https://hastebin.com/" + str.substring(8, str.length() - 2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}