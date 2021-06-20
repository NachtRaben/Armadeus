package dev.armadeus.core.managers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceEventManager extends InterfacedEventManager {

    private static final Map<Integer, ExecutorServiceEventManager> managers = new HashMap<>();

    public static ExecutorServiceEventManager get(int i) {
        return managers.computeIfAbsent(i, id -> new ExecutorServiceEventManager());
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("EventThread-%d").setDaemon(true).build());

    public void handle(Event event) {
        executor.submit(() -> super.handle(event));
    }
}