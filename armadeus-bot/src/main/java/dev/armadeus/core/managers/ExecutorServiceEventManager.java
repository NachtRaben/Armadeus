package dev.armadeus.core.managers;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceEventManager extends InterfacedEventManager {

    private final ExecutorService executor;

    public ExecutorServiceEventManager() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void handle(Event event) {
        executor.submit(() -> super.handle(event));
    }
}
