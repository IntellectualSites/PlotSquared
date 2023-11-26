/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
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
package com.plotsquared.bukkit.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.setup.PlotAreaBuilder;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

@Singleton
public class BukkitSetupUtils extends SetupUtils {

    private final PlotAreaManager plotAreaManager;
    private final YamlConfiguration worldConfiguration;
    private final File worldFile;

    @Inject
    public BukkitSetupUtils(
            final @NonNull PlotAreaManager plotAreaManager,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            @WorldFile final @NonNull File worldFile
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldConfiguration = worldConfiguration;
        this.worldFile = worldFile;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public void updateGenerators(final boolean force) {
        if (loaded && !SetupUtils.generators.isEmpty() && !force) {
            return;
        }
        String testWorld = "CheckingPlotSquaredGenerator";
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                if (plugin.isEnabled()) {
                    ChunkGenerator generator = plugin.getDefaultWorldGenerator(testWorld, "");
                    if (generator != null) {
                        PlotSquared.get().removePlotAreas(testWorld);
                        String name = plugin.getDescription().getName();
                        GeneratorWrapper<?> wrapped;
                        if (generator instanceof GeneratorWrapper<?>) {
                            wrapped = (GeneratorWrapper<?>) generator;
                        } else {
                            wrapped = new BukkitPlotGenerator(testWorld, generator, this.plotAreaManager);
                        }
                        SetupUtils.generators.put(name, wrapped);
                    }
                }
            } catch (Throwable e) { // Recover from third party generator error
                e.printStackTrace();
            }
        }
        loaded = true;
    }

    @Override
    public void unload(String worldName, boolean save) {
        TaskManager.runTask(() -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return;
            }
            Location location = Bukkit.getWorlds().get(0).getSpawnLocation();
            for (Player player : world.getPlayers()) {
                player.teleport(location);
            }
            if (save) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    chunk.unload(true);
                }
            } else {
                for (Chunk chunk : world.getLoadedChunks()) {
                    chunk.unload(false);
                }
            }
            Bukkit.unloadWorld(world, false);
        });
    }

    @Override
    public String setupWorld(PlotAreaBuilder builder) {
        this.updateGenerators(false);
        ConfigurationNode[] steps = builder.settingsNodesWrapper() == null ?
                new ConfigurationNode[0] : builder.settingsNodesWrapper().settingsNodes();
        String world = builder.worldName();
        PlotAreaType type = builder.plotAreaType();
        String worldPath = "worlds." + builder.worldName();
        switch (type) {
            case PARTIAL -> {
                if (builder.areaName() != null) {
                    if (!this.worldConfiguration.contains(worldPath)) {
                        this.worldConfiguration.createSection(worldPath);
                    }
                    ConfigurationSection worldSection =
                            this.worldConfiguration.getConfigurationSection(worldPath);
                    String areaName = builder.areaName() + "-" + builder.minimumId() + "-" + builder.maximumId();
                    String areaPath = "areas." + areaName;
                    if (!worldSection.contains(areaPath)) {
                        worldSection.createSection(areaPath);
                    }
                    ConfigurationSection areaSection =
                            worldSection.getConfigurationSection(areaPath);
                    HashMap<String, Object> options = new HashMap<>();
                    for (ConfigurationNode step : steps) {
                        options.put(step.getConstant(), step.getValue());
                    }
                    options.put("generator.type", builder.plotAreaType().toString());
                    options.put("generator.terrain", builder.terrainType().toString());
                    options.put("generator.plugin", builder.plotManager());
                    if (builder.generatorName() != null && !builder.generatorName()
                            .equals(builder.plotManager())) {
                        options.put("generator.init", builder.generatorName());
                    }
                    for (Entry<String, Object> entry : options.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (worldSection.contains(key)) {
                            Object current = worldSection.get(key);
                            if (!Objects.equals(value, current)) {
                                areaSection.set(key, value);
                            }
                        } else {
                            worldSection.set(key, value);
                        }
                    }
                }
                GeneratorWrapper<?> gen = SetupUtils.generators.get(builder.generatorName());
                if (gen != null && gen.isFull()) {
                    builder.generatorName(null);
                }
            }
            case AUGMENTED -> {
                if (!builder.plotManager().endsWith(":single")) {
                    if (!this.worldConfiguration.contains(worldPath)) {
                        this.worldConfiguration.createSection(worldPath);
                    }
                    if (steps.length != 0) {
                        ConfigurationSection worldSection =
                                this.worldConfiguration.getConfigurationSection(worldPath);
                        for (ConfigurationNode step : steps) {
                            worldSection.set(step.getConstant(), step.getValue());
                        }
                    }
                    this.worldConfiguration
                            .set("worlds." + world + ".generator.type", builder.plotAreaType().toString());
                    this.worldConfiguration
                            .set("worlds." + world + ".generator.terrain", builder.terrainType().toString());
                    this.worldConfiguration
                            .set("worlds." + world + ".generator.plugin", builder.plotManager());
                    if (builder.generatorName() != null && !builder.generatorName()
                            .equals(builder.plotManager())) {
                        this.worldConfiguration
                                .set("worlds." + world + ".generator.init", builder.generatorName());
                    }
                }
                GeneratorWrapper<?> gen = SetupUtils.generators.get(builder.generatorName());
                if (gen != null && gen.isFull()) {
                    builder.generatorName(null);
                }
            }
            case NORMAL -> {
                if (steps.length != 0) {
                    if (!this.worldConfiguration.contains(worldPath)) {
                        this.worldConfiguration.createSection(worldPath);
                    }
                    ConfigurationSection worldSection =
                            this.worldConfiguration.getConfigurationSection(worldPath);
                    for (ConfigurationNode step : steps) {
                        worldSection.set(step.getConstant(), step.getValue());
                    }
                }
            }
        }

        try {
            this.worldConfiguration.save(this.worldFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(PlotSquared.platform()).worldManager()
                .handleWorldCreation(builder.worldName(), builder.generatorName());

        if (Bukkit.getWorld(world) != null) {
            return world;
        }

        return builder.worldName();
    }

    @Override
    public String getGenerator(PlotArea plotArea) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators(false);
        }
        World world = Bukkit.getWorld(plotArea.getWorldName());
        if (world == null) {
            return null;
        }
        ChunkGenerator generator = world.getGenerator();
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
            GeneratorWrapper<?> current = entry.getValue();
            if (current.equals(generator)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
