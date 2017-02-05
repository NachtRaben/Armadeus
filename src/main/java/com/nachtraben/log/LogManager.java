package com.nachtraben.log;

import ch.qos.logback.classic.Logger;
import com.nachtraben.Tohsaka;
import net.dv8tion.jda.core.JDA;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 1/15/2017.
 */
public enum LogManager {

    TOHSAKA(Tohsaka.class),
    JDALOGGER(JDA.class);

    private static final String nachtraben = "118255810613608451";
    private static boolean sendMessage = true;
    private Logger logger;


    LogManager(Class clazz) {
        this.logger = (Logger) LoggerFactory.getLogger(clazz);
    }

    public void log(Level level, String message) {
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
            if (sendMessage) sendMessage(Level.ERROR, message);
            log(Level.ERROR, message);
        } else if (args.length == 1 && args[0] instanceof Throwable) {
            if (sendMessage) sendMessage(Level.ERROR, message, (Throwable) args[0]);
            logStackTrace(Level.ERROR, message, (Throwable) args[0]);
        } else {
            if (sendMessage) sendMessage(Level.ERROR, message, args);
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




    private static void sendMessage(Level level, String message, Object... args) {
//        if(!Tohsaka.instance.running) return;
//        User nacht = Tohsaka.instance.getJDA(0).getUserById(nachtraben);
//        boolean throwable = args.length != 0 && args[0] instanceof Throwable;
//        if (nacht != null) {
//            PrivateChannel channel = nacht.openPrivateChannel().complete();
//            EmbedBuilder m = new EmbedBuilder().setTitle(format("%s was encountered!", level.toString().toUpperCase()));
//            StringBuilder sb = new StringBuilder();
//            if(message != null) {
//                sb.append(format("__**Description:**__\n`%s`\n\n", format(message, args)));
//            } else if(throwable) {
//                sb.append("__**StackTrace:**__\n");
//                sb.append(formatStack((Throwable) args[0]));
//            }
//            m.setDescription(sb.toString());
//            m.setTimestamp(ZonedDateTime.now());
//            channel.sendMessage(m.build()).queue();
//        } else {
//            LogManager.TOHSAKA.warn("Attempted to send a message to nachtraben when he wasn't available!");
//        }
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

}


