package com.nachtraben.core.listeners;

import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by NachtRaben on 2/6/2017.
 */
public class JDALogListener implements SimpleLog.LogListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDALogListener.class);

    public JDALogListener() {
        SimpleLog.addListener(this);
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
    }

    @Override
    public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message) {
        switch (logLevel) {
            case ALL:
                break;
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
            case OFF:
                break;
        }
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        LOGGER.error(err.getMessage(), err);
    }
}
