package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

public class BukkitSetupUtils extends SetupUtils {

    @Override public void updateGenerators() {
        if (!SetupUtils.generators.isEmpty()) {
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
                            wrapped = new BukkitPlotGenerator(testWorld, generator);
                        }
                        SetupUtils.generators.put(name, wrapped);
                    }
                }
            } catch (Throwable e) { // Recover from third party generator error
                e.printStackTrace();
            }
        }
    }

    @Override public void unload(String worldName, boolean save) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }
        World dw = Bukkit.getWorlds().get(0);
        for (Player player : world.getPlayers()) {
            PaperLib.teleportAsync(player,dw.getSpawnLocation());
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
    }

    @Override public String setupWorld(SetupObject object) {
        SetupUtils.manager.updateGenerators();
        ConfigurationNode[] steps = object.step == null ? new ConfigurationNode[0] : object.step;
        String world = object.world;
        int type = object.type;
        String worldPath = "worlds." + object.world;
        switch (type) {
            case 2: {
                if (object.id != null) {
                    if (!PlotSquared.get().worlds.contains(worldPath)) {
                        PlotSquared.get().worlds.createSection(worldPath);
                    }
                    ConfigurationSection worldSection =
                        PlotSquared.get().worlds.getConfigurationSection(worldPath);
                    String areaName = object.id + "-" + object.min + "-" + object.max;
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
                    options.put("generator.type", object.type);
                    options.put("generator.terrain", object.terrain);
                    options.put("generator.plugin", object.plotManager);
                    if (object.setupGenerator != null && !object.setupGenerator
                        .equals(object.plotManager)) {
                        options.put("generator.init", object.setupGenerator);
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
                GeneratorWrapper<?> gen = SetupUtils.generators.get(object.setupGenerator);
                if (gen != null && gen.isFull()) {
                    object.setupGenerator = null;
                }
                break;
            }
            case 1: {
                if (!object.plotManager.endsWith(":single")) {
                    if (!PlotSquared.get().worlds.contains(worldPath)) {
                        PlotSquared.get().worlds.createSection(worldPath);
                    }
                    if (steps.length != 0) {
                        ConfigurationSection worldSection =
                            PlotSquared.get().worlds.getConfigurationSection(worldPath);
                        for (ConfigurationNode step : steps) {
                            worldSection.set(step.getConstant(), step.getValue());
                        }
                    }
                    PlotSquared.get().worlds
                        .set("worlds." + world + ".generator.type", object.type);
                    PlotSquared.get().worlds
                        .set("worlds." + world + ".generator.terrain", object.terrain);
                    PlotSquared.get().worlds
                        .set("worlds." + world + ".generator.plugin", object.plotManager);
                    if (object.setupGenerator != null && !object.setupGenerator
                        .equals(object.plotManager)) {
                        PlotSquared.get().worlds
                            .set("worlds." + world + ".generator.init", object.setupGenerator);
                    }
                }
                GeneratorWrapper<?> gen = SetupUtils.generators.get(object.setupGenerator);
                if (gen != null && gen.isFull()) {
                    object.setupGenerator = null;
                }
                break;
            }
            case 0: {
                if (steps.length != 0) {
                    if (!PlotSquared.get().worlds.contains(worldPath)) {
                        PlotSquared.get().worlds.createSection(worldPath);
                    }
                    ConfigurationSection worldSection =
                        PlotSquared.get().worlds.getConfigurationSection(worldPath);
                    for (ConfigurationNode step : steps) {
                        worldSection.set(step.getConstant(), step.getValue());
                    }
                }
                break;
            }
        }
        try {
            PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (object.setupGenerator != null) {
            if (plugin != null && plugin.isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "mv create " + world + " normal -g " + object.setupGenerator);
                setGenerator(world, object.setupGenerator);
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            WorldCreator wc = new WorldCreator(object.world);
            wc.generator(object.setupGenerator);
            wc.environment(Environment.NORMAL);
            wc.type(WorldType.FLAT);
            Bukkit.createWorld(wc);
            setGenerator(world, object.setupGenerator);
        } else {
            if (plugin != null && plugin.isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "mv create " + world + " normal");
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            World bw =
                Bukkit.createWorld(new WorldCreator(object.world).environment(Environment.NORMAL));
        }
        return object.world;
    }

    public void setGenerator(String world, String generator) {
        if (Bukkit.getWorlds().isEmpty() || !Bukkit.getWorlds().get(0).getName().equals(world)) {
            return;
        }
        File file = new File("bukkit.yml").getAbsoluteFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set("worlds." + world + ".generator", generator);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public String getGenerator(PlotArea plotArea) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        World world = Bukkit.getWorld(plotArea.worldname);
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
