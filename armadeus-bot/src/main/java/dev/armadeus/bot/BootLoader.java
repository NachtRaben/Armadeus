package dev.armadeus.bot;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import static java.util.Arrays.asList;

public class BootLoader {

    private static final Logger logger = LogManager.getLogger();

    private static Armadeus armadeus;

    public static void main(String... args) {
        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "help"), "Show the help");
                acceptsAll(asList("t", "token"), "Override bot token from CLI")
                        .withRequiredArg()
                        .ofType(String.class)
                        .describedAs("token");
                acceptsAll(Collections.singletonList("dev-mode"), "Puts the application in developer mode");
                acceptsAll(asList("v", "version"), "Show the application version");
            }
        };

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch(OptionException e) {
            logger.error("Failed to parse CLI arguments", e);
        }

        if(options == null || options.has("?")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                logger.error(e);
            }
        } else if(options.has("v")) {
            // TODO: Print application version to console
        } else {
            // Print JVM environment
            java.lang.management.RuntimeMXBean runtimeMX = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.management.OperatingSystemMXBean osMX = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (runtimeMX != null && osMX != null) {
                String javaInfo = "Java " + runtimeMX.getSpecVersion() + " (" + runtimeMX.getVmName() + " " + runtimeMX.getVmVersion() + ")";
                String osInfo = "Host:  " + osMX.getName() + " " + osMX.getVersion() + " (" + osMX.getArch() + ")";

                logger.info("System Info: " + javaInfo + " " + osInfo);
            } else {
                logger.warn("Unable to read system info");
            }
            armadeus = new Armadeus(options);
            armadeus.start();
        }


    }

}
