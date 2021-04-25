/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.impl.plugin;

import co.aikar.commands.CommandManager;
import com.google.common.base.Joiner;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.meta.PluginDependency;
import com.velocitypowered.impl.plugin.loader.VelocityPluginContainer;
import com.velocitypowered.impl.plugin.loader.java.JavaPluginLoader;
import com.velocitypowered.impl.plugin.loader.java.JavaVelocityPluginDescriptionCandidate;
import com.velocitypowered.impl.plugin.util.PluginDependencyUtils;
import dev.armadeus.bot.api.ArmaCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class VelocityPluginManager implements PluginManager {

    private static final Logger logger = LogManager.getLogger(VelocityPluginManager.class);

    private final Map<String, PluginContainer> plugins = new LinkedHashMap<>();
    private final Map<Object, PluginContainer> pluginInstances = new IdentityHashMap<>();
    private final ArmaCore core;

    public VelocityPluginManager(ArmaCore core) {
        this.core = checkNotNull(core, "server");
    }

    /**
     * Loads all plugins from the specified {@code directory}.
     * @param directory the directory to load from
     * @throws IOException if we could not open the directory
     */
//  @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
//      justification = "I looked carefully and there's no way SpotBugs is right.")
    public void loadPlugins(Path directory) throws IOException {
        checkNotNull(directory, "directory");
        checkArgument(directory.toFile().isDirectory(), "provided path isn't a directory");

        List<PluginDescription> found = new ArrayList<>();

        JavaVelocityPluginDescriptionCandidate fakeDesk = new JavaVelocityPluginDescriptionCandidate(
                "armacore", "ArmaCore", "0.1", "Dummy plugin for ArmaCore", "https://armadeus.net", Collections.singletonList("NachtRaben"),
                Collections.emptyList(), Files.createTempDirectory(""), "dev.armadeus.core.plugin.ArmaCorePlugin");
        found.add(fakeDesk);

        JavaPluginLoader loader = new JavaPluginLoader(core, directory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory,
                p -> p.toFile().isFile() && p.toString().endsWith(".jar"))) {
            for (Path path : stream) {
                try {
                    found.add(loader.loadPluginDescription(path));
                } catch (Exception e) {
                    logger.error("Unable to load plugin {}", path, e);
                }
            }
        }

        if (found.isEmpty()) {
            // No plugins found
            return;
        }

        List<PluginDescription> sortedPlugins = PluginDependencyUtils.sortCandidates(found);

        Set<String> loadedPluginsById = new HashSet<>();
        Map<PluginContainer, Module> pluginContainers = new LinkedHashMap<>();
        // Now load the plugins
        pluginLoad:
        for (PluginDescription candidate : sortedPlugins) {
            // Verify dependencies
            for (PluginDependency dependency : candidate.dependencies()) {
                if (!dependency.isOptional() && !loadedPluginsById.contains(dependency.getId())) {
                    logger.error("Can't load plugin {} due to missing dependency {}", candidate.id(),
                            dependency.getId());
                    continue pluginLoad;
                }
            }

            try {
                PluginDescription realPlugin = loader.loadPlugin(candidate);
                VelocityPluginContainer container = new VelocityPluginContainer(realPlugin);
                pluginContainers.put(container, loader.createModule(container));
                loadedPluginsById.add(realPlugin.id());
            } catch (Exception e) {
                logger.error("Can't create module for plugin {}", candidate.id(), e);
            }
        }

        // Make a global Guice module that with common bindings for every plugin
        AbstractModule commonModule = new AbstractModule() {

            @Override
            protected void configure() {
                bind(ArmaCore.class).toInstance(core);
                bind(PluginManager.class).toInstance(core.getPluginManager());
                bind(EventManager.class).toInstance(core.getEventManager());
                bind(CommandManager.class).toInstance(core.getCommandManager());
                for (PluginContainer container : pluginContainers.keySet()) {
                    bind(PluginContainer.class)
                            .annotatedWith(Names.named(container.description().id()))
                            .toInstance(container);
                }
            }
        };

        for (Map.Entry<PluginContainer, Module> plugin : pluginContainers.entrySet()) {
            PluginContainer container = plugin.getKey();
            PluginDescription description = container.description();

            try {
                loader.createPlugin(container, plugin.getValue(), commonModule);
            } catch (Exception e) {
                logger.error("Can't create plugin {}", description.id(), e);
                continue;
            }

            logger.info("Loaded plugin {} {} by {}", description.id(), description.version()
                    .orElse("<UNKNOWN>"), Joiner.on(", ").join(description.authors()));
            registerPlugin(container);
        }
    }

    public void registerPlugin(PluginContainer plugin) {
        plugins.put(plugin.description().id(), plugin);
        Optional<?> instance = plugin.instance();
        instance.ifPresent(o -> pluginInstances.put(o, plugin));
    }

    @Override
    public Optional<PluginContainer> fromInstance(Object instance) {
        checkNotNull(instance, "instance");

        if (instance instanceof PluginContainer) {
            return Optional.of((PluginContainer) instance);
        }

        return Optional.ofNullable(pluginInstances.get(instance));
    }

    @Override
    public Optional<PluginContainer> getPlugin(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(plugins.get(id));
    }

    @Override
    public Collection<PluginContainer> plugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    @Override
    public boolean isLoaded(String id) {
        return plugins.containsKey(id);
    }

    @Override
    public void addToClasspath(Object plugin, Path path) {
        checkNotNull(plugin, "instance");
        checkNotNull(path, "path");
        Optional<PluginContainer> optContainer = fromInstance(plugin);
        checkArgument(optContainer.isPresent(), "plugin is not loaded");
        Optional<?> optInstance = optContainer.get().instance();
        checkArgument(optInstance.isPresent(), "plugin has no instance");

        ClassLoader pluginClassloader = optInstance.get().getClass().getClassLoader();
        if (pluginClassloader instanceof PluginClassLoader) {
            ((PluginClassLoader) pluginClassloader).addPath(path);
        } else {
            throw new UnsupportedOperationException(
                    "Operation is not supported on non-Java Velocity plugins.");
        }
    }
}
