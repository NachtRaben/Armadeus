package com.nachtraben.core.configuration;

import com.google.gson.JsonElement;

/**
 * Created by NachtRaben on 2/13/2017.
 */
public interface JsonIO {
    JsonElement write();
    void read(JsonElement me);
    default void onCreate() {}
}
