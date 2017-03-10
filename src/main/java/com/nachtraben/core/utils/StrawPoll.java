package com.nachtraben.core.utils;
// Created by Xilixir on 2017-02-06

import com.nachtraben.core.configuration.JsonLoader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class StrawPoll {
    private StrawObject straw;

    public StrawPoll(String[] options, String title) throws IOException, JSONException {
        HttpURLConnection con = (HttpURLConnection) new URL("https://strawpoll.me/api/v2/polls").openConnection();
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "application/json");
        con.addRequestProperty("User-Agent", "Mozilla/5.0");

        con.setDoOutput(true);

        JSONObject json = new JSONObject().put("title", title).put("options", options);
        String jsonStr = json.toString();
        con.addRequestProperty("Content-Length", Integer.toString(jsonStr.length()));

        DataOutputStream data = new DataOutputStream(con.getOutputStream());
        data.writeBytes(jsonStr);
        data.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        this.straw = JsonLoader.GSON_P.fromJson(response.toString(), StrawObject.class);
    }

    public StrawObject getStraw() {
        return straw;
    }

    public class StrawObject {
        private List<String> options;
        private String title;
        private String dupcheck;
        private boolean multi;
        private boolean captcha;
        private long pc;
        private long id;

        public StrawObject(List<String> options, String title, String dupcheck, boolean multi, boolean captcha, long pc, long id) {
            this.options = options;
            this.title = title;
            this.dupcheck = dupcheck;
            this.multi = multi;
            this.captcha = captcha;
            this.pc = pc;
            this.id = id;
        }

        public String getURL(){
            return "http://www.strawpoll.me/" + this.id;
        }

        public List<String> getOptions() {
            return options;
        }

        public String getTitle() {
            return title;
        }

        public String getDupcheck() {
            return dupcheck;
        }

        public boolean isMulti() {
            return multi;
        }

        public boolean isCaptcha() {
            return captcha;
        }

        public long getPc() {
            return pc;
        }

        public long getId() {
            return id;
        }
    }
}
