package com.nachtraben;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.nachtraben.core.commandmodule.CommandBase;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.utils.LogManager;
import com.nachtraben.core.utils.LogbackListener;
import com.nachtraben.tohsaka.Tohsaka;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * Created by NachtRaben on 2/1/2017.
 */
public class TohsakaTest implements CommandSender {

    Tohsaka tohsaka;
    CommandBase commandBase;

    public TohsakaTest() {
        registerLogListener();
        tohsaka = new Tohsaka(true);
        //HasteBin test = new HasteBin("test", "xml");
        //runCommand("test", new String[]{ "moo", "uno", "test"});
    }

    private void registerLogListener() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg");
        ple.setContext(lc);
        ple.start();
        LogbackListener<ILoggingEvent> listener = new LogbackListener<>();
        listener.setContext(lc);
        listener.start();

        LogManager.ROOT.getLogger().addAppender(listener);
    }

    public static void main(String... args) {
        new TohsakaTest();
    }

    @Override
    public void sendMessage(String s) {

    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public String getName() {
        return "TohsakaTest";
    }

    @Override
    public Future<Void> runCommand(String command, String[] args) {
        commandBase.execute(this, command, args);
        return null;
    }
}
