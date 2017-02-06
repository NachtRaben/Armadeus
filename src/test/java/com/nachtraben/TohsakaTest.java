package com.nachtraben;

import com.xilixir.fw.BotFramework;

import java.util.Arrays;

/**
 * Created by NachtRaben on 2/1/2017.
 */
public class TohsakaTest {

    Tohsaka tohsaka;

    public TohsakaTest() {
        tohsaka = new Tohsaka(false);
        BotFramework.COMMAND_PREFIXES = Arrays.asList("-", "?");
    }

    public static void main(String... args) {
        new TohsakaTest();
    }

}
