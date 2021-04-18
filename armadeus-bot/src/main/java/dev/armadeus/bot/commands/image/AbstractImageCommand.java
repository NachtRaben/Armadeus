package dev.armadeus.bot.commands.image;

import dev.armadeus.core.util.TimedCache;
import dev.armadeus.command.command.Command;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class AbstractImageCommand extends Command {

    protected static final Random RAND = new Random();
    private static final Logger logger = LogManager.getLogger();

    protected TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    protected TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);

    /**
     * Instantiates a new Command.
     *
     * @param name        the name
     * @param format      the format
     * @param description the description
     */
    public AbstractImageCommand(String name, String format, String description) {
        super(name, format, description);
    }
}
