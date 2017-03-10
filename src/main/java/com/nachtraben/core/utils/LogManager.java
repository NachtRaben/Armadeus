package com.nachtraben.core.utils;

import ch.qos.logback.classic.Logger;
import com.nachtraben.core.JDABot;
import com.nachtraben.core.configuration.JsonLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.nachtraben.core.utils.StringUtils.format;

/**
 * Created by NachtRaben on 1/15/2017.
 */
public enum LogManager {
    JDALOGGER("JDA"),
    TOHSAKA("TOHSAKA"),
    ROOT(Logger.ROOT_LOGGER_NAME);
    private Logger logger;
    private Calendar cal = Calendar.getInstance();
    private SimpleDateFormat dataformat = new SimpleDateFormat("HH:mm:ss");


    LogManager(String name) {
        this.logger = (Logger) LoggerFactory.getLogger(name);
    }

    public void logJson(Object object){
        System.out.println(JsonLoader.GSON_P.toJson(object));
    }

    public void log(Level level, String message) {
        if(JDABot.getInstance().getGlobalLogChannel() != null)
            //JDABot.getInstance().getGlobalLogChannel().sendMessage(format("[%s][%s]: %s", dataformat.format(cal.getTime()), level.toString(), message)).queue();
        switch (level) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case TRACE:
                logger.trace(message);
                break;
        }
    }

    public void logStackTrace(Level level, String message, Throwable stack) {
        JDABot.getInstance().getGlobalLogChannel().sendMessage(format("[%s][%s]: %s\n```java\n%s\n```", dataformat.format(cal.getTime()), level.toString(), message, formatStack(stack))).queue();
        switch (level) {
            case ERROR:
                logger.error(message, stack);
                break;
            case WARN:
                logger.warn(message, stack);
                break;
            case INFO:
                logger.info(message, stack);
                break;
            case DEBUG:
                logger.debug(message, stack);
                break;
            case TRACE:
                logger.trace(message, stack);
                break;
        }
    }

    public void error(String message, Object... args) {
        if (args.length == 0) {
            log(Level.ERROR, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            logStackTrace(Level.ERROR, message, (Throwable) args[0]);
        } else {
            log(Level.ERROR, format(message, args));
        }
    }

    public void warn(String message, Object... args) {
        if (args.length == 0) {
            log(Level.WARN, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            logStackTrace(Level.WARN, message, (Throwable) args[0]);
        } else {
            log(Level.WARN, format(message, args));
        }
    }

    public void info(String message, Object... args) {
        if (args.length == 0) {
            log(Level.INFO, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            logStackTrace(Level.INFO, message, (Throwable) args[0]);
        } else {
            log(Level.INFO, format(message, args));
        }
    }

    public void debug(String message, Object... args) {
        if (args.length == 0) {
            log(Level.DEBUG, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            logStackTrace(Level.DEBUG, message, (Throwable) args[0]);
        } else {
            log(Level.DEBUG, format(message, args));
        }
    }

    public void trace(String message, Object... args) {
        if (args.length == 0) {
            log(Level.TRACE, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            logStackTrace(Level.TRACE, message, (Throwable) args[0]);
        } else {
            log(Level.TRACE, format(message, args));
        }
    }

    private static String formatStack(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String stack = sw.toString();
        int start = 0;
        int end = stack.length();
        if (t.getCause() != null) {
            start = stack.lastIndexOf(t.getCause().toString());
        }
        StringBuilder sb = new StringBuilder();
        if (t.getMessage() != null) {
            sb.append("-Message: ").append("`").append(t.getLocalizedMessage()).append("`\n");
        }
        if (t.getCause() != null) {
            sb.append("-Cause: ").append("`").append(t.getCause()).append("`\n");
        }
        sb.append("```java\n");
        sb.append(stack.substring(start, end));
        sb.append("\n```");
        return sb.toString();
    }

    public Logger getLogger() {
        return logger;
    }

}


