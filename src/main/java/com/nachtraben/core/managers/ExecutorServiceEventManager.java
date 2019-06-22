package com.nachtraben.core.managers;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceEventManager extends InterfacedEventManager {

    private final ExecutorService executor;

    public ExecutorServiceEventManager() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void handle(Event event) {
        executor.submit(() -> super.handle(event));
    }
}
