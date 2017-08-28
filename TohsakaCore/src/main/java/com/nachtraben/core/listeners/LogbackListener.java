package com.nachtraben.core.listeners;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;


public class LogbackListener<E> extends AppenderBase<E> {

    private DiscordBot bot;

    public LogbackListener(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    protected void append(E eventObject) {
        if (eventObject instanceof ILoggingEvent) {
            ILoggingEvent event = (ILoggingEvent) eventObject;
            Level level = event.getLevel();
            if(level.isGreaterOrEqual(Level.ERROR)) {
                TextChannel channel = bot.getConfig().getErrorLogChannel();
                if(channel != null) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(String.format("[%s]: %s", level.levelStr, event.getMessage()));
                    if(event.getThrowableProxy() != null) {
                        eb.addField("Stack:", getStackTrace(event), false);
                    }
                    eb.setFooter(new Date(event.getTimeStamp()).toString(), null);
                    channel.sendMessage(eb.build()).queue();
                }
            }
        }
    }

    private String getStackTrace(ILoggingEvent event) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ThrowableProxy e = (ThrowableProxy) event.getThrowableProxy();
        e.getThrowable().printStackTrace(pw);
        return sw.toString();
    }

    public static void install(DiscordBot bot) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg");
        ple.setContext(lc);
        ple.start();
        LogbackListener<ILoggingEvent> listener = new LogbackListener<>(bot);
        listener.setContext(lc);
        listener.start();
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(listener);
    }

}