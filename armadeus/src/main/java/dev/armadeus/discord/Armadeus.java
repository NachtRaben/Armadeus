package dev.armadeus.discord;

import co.aikar.commands.CommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.Velocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Plugin(id = "armadeus", name = "Armadeus", version = "MASTER", url = "https://armadeus.com", description = "Utility Discord Bot", authors = { "NachtRaben" })
public class Armadeus {

    private static final Logger logger = LoggerFactory.getLogger("Armadeus");

    private final ConcurrentHashMap<Long, Map<Long, Long>> cooldowns = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<Long, Long> pings = new ConcurrentHashMap<>();

    private ArmaCore core;

    @Inject
    public Armadeus(Velocity core) {
        this.core = (ArmaCore) core;
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(Armadeus.class.getClassLoader())
                .setScanners(new SubTypesScanner())
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.discord", Armadeus.class.getClassLoader()))
                .filterInputsBy(new FilterBuilder().includePackage("dev.armadeus.discord"))
        );
        Set<Class<? extends DiscordCommand>> commandClazzes = reflections.getSubTypesOf(DiscordCommand.class);
        commandClazzes.forEach(clazz -> {
            try {
                if (!clazz.isSynthetic() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    logger.info("Registering command class {}", clazz.getSimpleName());
                    manager.registerCommand(clazz.getConstructor().newInstance());
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                logger.error("Failed to register command class", e);
            }
        });
    }
}
