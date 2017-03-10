package com.nachtraben.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;


/**
 * Created by NachtRaben on 2/13/2017.
 */
public class JsonLoader {

    public static final Gson GSON_P;
    public static final File BASE_DIR = new File("./");
    public static final File GUILD_DIR = new File(BASE_DIR, "guilds");

    static {
        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();
        gb.disableHtmlEscaping();
        GSON_P = gb.create();
    }

    public static <T extends JsonIO> T loadFile(File dataDir, String filename, T data) {
        if(dataDir == null || filename == null || data == null) throw new IllegalArgumentException("One of the parameters was null!");
        if(!dataDir.exists()) throw new IllegalArgumentException("Provided data dir must exist!");
        File target = new File(dataDir, filename);
        if(target.exists()) {
            FileReader in;
            try {
                in = new FileReader(target);
                JsonReader reader = new JsonReader(in);
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(reader);
                data.read(element);
                return data;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if(target.createNewFile()) {
                    saveFile(dataDir, filename, data);
                } else {
                    //TODO: Error writing file.
                }
                data.onCreate();
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static <T extends JsonIO> void saveFile(File dataDir, String filename, T data) {
        if(dataDir == null || filename == null || data == null) throw new IllegalArgumentException("One of the parameters was null!");
        try {
            File target = new File(dataDir, filename);
            FileWriter writer = new FileWriter(target);
            GSON_P.toJson(data.write(), writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
