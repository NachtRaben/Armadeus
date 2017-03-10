package com.nachtraben.core.listeners;

import com.nachtraben.core.utils.LogManager;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * Created by NachtRaben on 2/6/2017.
 */
public class JDALogListener implements SimpleLog.LogListener {

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
                LogManager.JDALOGGER.trace(message.toString());
                break;
            case DEBUG:
                LogManager.JDALOGGER.debug(message.toString());
                break;
            case INFO:
                LogManager.JDALOGGER.info(message.toString());
                break;
            case WARNING:
                LogManager.JDALOGGER.warn(message.toString());
                break;
            case FATAL:
                LogManager.JDALOGGER.error(message.toString());
                break;
            case OFF:
                break;
        }
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        LogManager.JDALOGGER.error(err.getMessage(), err);
    }
}
