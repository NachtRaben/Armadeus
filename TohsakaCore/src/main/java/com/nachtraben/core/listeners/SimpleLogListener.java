package com.nachtraben.core.listeners;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLogListener implements SimpleLog.LogListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDA.class);
    private static SimpleLogListener instance;

    public static void init() {
        if(instance != null)
            throw new IllegalStateException("The SimpleLogListener has already been initialized!");

        instance = new SimpleLogListener();
        SimpleLog.addListener(instance);
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        LOGGER.info("Logging now being redirected to slf4j.");
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    @Override
    public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message) {
        switch (logLevel) {
            case TRACE:
                LOGGER.trace(message.toString());
                break;
            case DEBUG:
                LOGGER.debug(message.toString());
                break;
            case INFO:
                LOGGER.info(message.toString());
                break;
            case WARNING:
                LOGGER.warn(message.toString());
                break;
            case FATAL:
                LOGGER.error(message.toString());
                break;
        }
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        LOGGER.error(err.getMessage(), err);
    }
}
