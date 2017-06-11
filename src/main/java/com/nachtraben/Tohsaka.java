package com.nachtraben;

import com.nachtraben.core.DiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tohsaka extends DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tohsaka.class);

    private static Tohsaka instance;

    public Tohsaka() {
        instance = this;

    }

    public static Tohsaka getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        DiscordBot.PROGRAM_ARGS = args;
        new Tohsaka();
    }

}
