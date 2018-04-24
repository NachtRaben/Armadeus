package com.nachtraben.tohsaka.commands.image;

import com.nachtraben.core.util.TimedCache;
import com.nachtraben.orangeslice.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class AbstractImageCommand extends Command {

    protected static final Random RAND = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageCommand.class);

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
