package dev.armadeus.core;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import static java.util.Arrays.asList;

public class BootLoader {

    private static final Logger logger;

    static {
        // Override LUL LogManager with an Log4J2 LogManager
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        logger = LogManager.getLogger();
    }

    public static void main(String[] args) {
        // Standard CLI arguments parser
        OptionParser parser = new OptionParser() {{
            acceptsAll(asList("?", "help"), "Show the help");
            acceptsAll(asList("t", "token"), "Override bot token from CLI")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("token");
            acceptsAll(Collections.singletonList("dev-mode"), "Puts the application in developer mode");
            acceptsAll(asList("v", "version"), "Show the application version");
        }};

        // Parse CLI args
        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            logger.error("Failed to parse CLI arguments", e);
            return;
        }

        // Print help if CLI has "?"
        if (options.has("?")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                logger.error(e);
            }
            return;
        }

        // Print implementation if CLI has "v"
        if (options.has("v")) {
            // TODO: Print application version to console
            return;
        }

        // Print JVM environment to console for debugging purposes
        java.lang.management.RuntimeMXBean runtimeMX = java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.lang.management.OperatingSystemMXBean osMX = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        if (runtimeMX != null && osMX != null) {
            String javaInfo = "Java " + runtimeMX.getSpecVersion() + " (" + runtimeMX.getVmName() + " " + runtimeMX.getVmVersion() + ")";
            String osInfo = "Host:  " + osMX.getName() + " " + osMX.getVersion() + " (" + osMX.getArch() + ")";
            logger.info("System Info: " + javaInfo + " " + osInfo);
        } else {
            logger.warn("Unable to read System information");
        }

        long startTime = System.currentTimeMillis();
        ArmaCoreImpl core = new ArmaCoreImpl(options);
        core.start();
        Runtime.getRuntime().addShutdownHook(new Thread(core::shutdown, "Shutdown Thread"));
        double bootTime = (System.currentTimeMillis() - startTime) / 1000D;
        logger.info("Loaded in ({}s)", bootTime);
    }
}
