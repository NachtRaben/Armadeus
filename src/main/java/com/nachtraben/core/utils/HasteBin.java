package com.nachtraben.core.utils;

import com.mashape.unirest.http.Unirest;
import junit.framework.Assert;
import org.json.JSONObject;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class HasteBin {
    private static final String HASTE_URL = "https://haste.nachtraben.com/documents";
    private String url;


    public HasteBin(String content) {
        this(content, "");
    }

    public HasteBin(String content, String extension) {
        Assert.assertFalse("Haste cannot exceed 40k characters", content.length() > 40000);
        this.url = null;
        try {
            JSONObject repsonse = Unirest.post(HASTE_URL).body(content).asJson().getBody().getObject();
            if(repsonse.has("key")) {
                url = HASTE_URL.replace("documents", "") + repsonse.get("key");
                if(extension != null && !extension.isEmpty())
                    url = url + (extension.startsWith(".") ? extension : "." + extension);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getHaste() {
        return this.url;
    }
}