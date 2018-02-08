package com.nachtraben.core.command;

import com.nachtraben.core.util.Utils;
import com.nachtraben.logback.TerminalConsoleAdaptor;
import com.nachtraben.orangeslice.CommandResult;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.tohsaka.Tohsaka;
import org.apache.http.util.Asserts;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Future;

public class ConsoleCommandSender implements CommandSender, Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsoleCommandSender.class);

    private static ConsoleCommandSender instance;
    private static Scanner input;
    private static LineReader reader;
    private static boolean stopped = true;

    static {
        instance = new ConsoleCommandSender();
    }

    protected ConsoleCommandSender() {
        Asserts.check(instance == null, "There is already an instance of the ConsoleCommandSender");
        instance = this;
    }

    public static void start() {
        Asserts.check(stopped, "The ConsoleCommandSender has already been started.");
        stopped = false;
        initTerminal();
        log.info("Console initialized and started.");
        Utils.getExecutor().execute(instance);
    }

    public static void stop() {
        Asserts.check(!stopped, "The ConsoleCommandSender isn't started.");
        stopped = true;
        log.info("Console has been shutdown.");
    }

    private static void initTerminal() {
        TerminalConsoleAdaptor.initializeTerminal();
        if (TerminalConsoleAdaptor.getTerminal() != null) {
            reader = LineReaderBuilder.builder().appName("Terminal").terminal(TerminalConsoleAdaptor.getTerminal()).build();
            reader.unsetOpt(LineReader.Option.INSERT_TAB);
            TerminalConsoleAdaptor.setReader(reader);
        } else {
            input = new Scanner(System.in);
        }
    }

    @Override
    public void sendMessage(String s) {
        log.info("MESSAGE: " + s);
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public Future<CommandResult> runCommand(String command, String[] args) {
        return Tohsaka.getInstance().getCommandBase().execute(this, command, args);

    }

    @Override
    public void run() {
        String message;
        while (!stopped) {
            if (reader != null) {
                try {
                    String line;
                    log.info("Started.");
                    while (true) {
                        try {
                            line = reader.readLine("> ");
                        } catch (EndOfFileException e) {
                            continue;
                        }

                        if (line == null)
                            break;

                        line = line.trim();
                        if (!line.isEmpty()) {
                            String[] tokens = line.split("\\s+");
                            String command = tokens[0];
                            String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                            runCommand(command, args);
                        }
                    }
                } catch (UserInterruptException e) {
                    System.exit(0);
                } finally {
                    reader = null;
                    TerminalConsoleAdaptor.setReader(null);
                }
            } else if (input != null) {
                while ((message = input.nextLine()) != null) {
                    String[] tokens = message.split("\\s+");
                    String command = tokens[0];
                    String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                    runCommand(command, args);
                }
            } else {
                log.error("The LineReader and Scanner are both null! Wtf? Closing thread.");
                break;
            }
        }
    }

}
