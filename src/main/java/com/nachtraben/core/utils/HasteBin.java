package com.nachtraben.core.utils;

import junit.framework.Assert;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class HasteBin {
    private String url;

    public HasteBin(String content) {
        this(content, "");
    }

    public HasteBin(String content, String extension) {
        Assert.assertFalse("Haste cannot exceed 40k characters", content.length() > 40000);
        this.url = null;
        try {
            URL url = new URL("https://haste.nachtraben.com/documents");
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.addRequestProperty("user-agent", "Tohsaka");
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
            //System.out.println(sb.toString());
            this.url = "https://haste.nachtraben.com/" + str.substring(8, str.length() - 2);
            if(extension != null && !extension.isEmpty()) this.url = this.url + (extension.startsWith(".") ? extension : "." + extension);
            System.out.println(this.url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getHaste() {
        return this.url;
    }
}