package dev.armadeus.core;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class BootLoader {

    private static DiscordBot source;

    public static void main(String... args) {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        Logger logger = LogManager.getLogger();
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

            Reflections reflections = new Reflections("");
            Set<Class<? extends DiscordBot>> canidates = reflections.getSubTypesOf(DiscordBot.class);
            if (canidates.size() > 1) {
                logger.error("Failed to initialize, multiple launch candidates found!");
                logger.error(canidates.stream().map(Class::getName).collect(Collectors.joining(",")));
            } else if (canidates.size() != 1) {
                logger.error("No launch candidates found! Launch class must extend {}", DiscordBot.class.getName());
            }

            Class<? extends DiscordBot> clazz = null;
            try {
                clazz = canidates.stream().findFirst().orElseThrow();
                Constructor<? extends DiscordBot> constructor = clazz.getDeclaredConstructor(OptionSet.class);
                logger.info("Discovered entrypoint {}", clazz.getName());
                source = constructor.newInstance(options);
                source.start();
            } catch (NoSuchMethodException e) {
                logger.error("Failed to launch, {} is missing required constructor arg {}", clazz, OptionSet.class);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                assert clazz != null;
                logger.error("Failed to launch " + clazz.getName(), e);
            }

        }


    }

}
