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

package com.velocitypowered.impl.plugin.loader.java;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.velocitypowered.api.plugin.InvalidPluginException;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.ap.SerializedPluginDescription;
import com.velocitypowered.api.plugin.meta.PluginDependency;
import com.velocitypowered.impl.plugin.PluginClassLoader;
import com.velocitypowered.impl.plugin.loader.VelocityPluginContainer;
import com.velocitypowered.proxy.plugin.loader.PluginLoader;
import com.velocitypowered.proxy.plugin.loader.VelocityPluginDescription;
import dev.armadeus.bot.api.ArmaCore;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JavaPluginLoader implements PluginLoader {

  private final ArmaCore core;
  private final Path baseDirectory;

  public JavaPluginLoader(ArmaCore core, Path baseDirectory) {
    this.core = core;
    this.baseDirectory = baseDirectory;
  }

  @Override
  public PluginDescription loadPluginDescription(Path source) throws Exception {
    Optional<SerializedPluginDescription> serialized = getSerializedPluginInfo(source);

    if (serialized.isEmpty()) {
      throw new InvalidPluginException("Did not find a valid velocity-plugin.json.");
    }

    SerializedPluginDescription pd = serialized.get();
    if (!SerializedPluginDescription.ID_PATTERN.matcher(pd.getId()).matches()) {
      throw new InvalidPluginException("Plugin ID '" + pd.getId() + "' is invalid.");
    }

    return createCandidateDescription(pd, source);
  }

  @Override
  public PluginDescription loadPlugin(PluginDescription source) throws Exception {
    if (!(source instanceof JavaVelocityPluginDescriptionCandidate)) {
      throw new IllegalArgumentException("Description provided isn't of the Java plugin loader");
    }

    URL pluginJarUrl = source.file().get().toUri().toURL();
    PluginClassLoader loader = AccessController.doPrivileged(
        (PrivilegedAction<PluginClassLoader>) () -> new PluginClassLoader(new URL[]{pluginJarUrl}));
    loader.addToClassloaders();

    JavaVelocityPluginDescriptionCandidate candidate = (JavaVelocityPluginDescriptionCandidate) source;
    Class mainClass = loader.loadClass(candidate.getMainClass());
    return createDescription(candidate, mainClass);
  }

  @Override
  public Module createModule(PluginContainer container) throws Exception {
    PluginDescription description = container.description();
    if (!(description instanceof JavaVelocityPluginDescription)) {
      throw new IllegalArgumentException("Description provided isn't of the Java plugin loader");
    }

    JavaVelocityPluginDescription javaDescription = (JavaVelocityPluginDescription) description;
    Optional<Path> source = javaDescription.file();

    if (!source.isPresent()) {
      throw new IllegalArgumentException("No path in plugin description");
    }

    return new VelocityPluginModule(core, javaDescription, container, baseDirectory);
  }

  @Override
  public void createPlugin(PluginContainer container, Module... modules) {
    if (!(container instanceof VelocityPluginContainer)) {
      throw new IllegalArgumentException("Container provided isn't of the Java plugin loader");
    }
    PluginDescription description = container.description();
    if (!(description instanceof JavaVelocityPluginDescription)) {
      throw new IllegalArgumentException("Description provided isn't of the Java plugin loader");
    }

    Injector injector = Guice.createInjector(modules);
    Object instance = injector.getInstance(((JavaVelocityPluginDescription) description).getMainClass());

    if (instance == null) {
      throw new IllegalStateException(
        "Got nothing from injector for plugin " + description.id());
    }

    ((VelocityPluginContainer) container).setInstance(instance);
  }

  private Optional<SerializedPluginDescription> getSerializedPluginInfo(Path source)
      throws Exception {
    try (JarInputStream in = new JarInputStream(
        new BufferedInputStream(Files.newInputStream(source)))) {
      JarEntry entry;
      while ((entry = in.getNextJarEntry()) != null) {
        if (entry.getName().equals("velocity-plugin.json")) {
          try (Reader pluginInfoReader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return Optional.of(ArmaCore.GENERAL_GSON.fromJson(pluginInfoReader,
                SerializedPluginDescription.class));
          }
        }
      }
      return Optional.empty();
    }
  }

  private VelocityPluginDescription createCandidateDescription(
      SerializedPluginDescription description,
      Path source) {
    Set<PluginDependency> dependencies = new HashSet<>();

    for (SerializedPluginDescription.Dependency dependency : description.getDependencies()) {
      dependencies.add(toDependencyMeta(dependency));
    }

    return new JavaVelocityPluginDescriptionCandidate(
        description.getId(),
        description.getName(),
        description.getVersion(),
        description.getDescription(),
        description.getUrl(),
        description.getAuthors(),
        dependencies,
        source,
        description.getMain()
    );
  }

  private VelocityPluginDescription createDescription(
      JavaVelocityPluginDescriptionCandidate description,
      Class mainClass) {
    return new JavaVelocityPluginDescription(
        description.id(),
        description.name().orElse(null),
        description.version().orElse(null),
        description.description().orElse(null),
        description.url().orElse(null),
        description.authors(),
        description.dependencies(),
        description.file().orElse(null),
        mainClass
    );
  }

  private static PluginDependency toDependencyMeta(
      SerializedPluginDescription.Dependency dependency) {
    return new PluginDependency(
        dependency.getId(),
        null, // TODO Implement version matching in dependency annotation
        dependency.isOptional()
    );
  }
}
