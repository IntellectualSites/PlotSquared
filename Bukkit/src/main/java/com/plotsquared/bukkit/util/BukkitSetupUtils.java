package com.plotsquared.bukkit.util;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

public class BukkitSetupUtils extends SetupUtils {

    @Override
    public void updateGenerators() {
        if (!SetupUtils.generators.isEmpty()) {
            return;
        }
        String testWorld = "CheckingPlotSquaredGenerator";
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.isEnabled()) {
                ChunkGenerator generator = plugin.getDefaultWorldGenerator(testWorld, "");
                if (generator != null) {
                    PS.get().removePlotAreas(testWorld);
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
        }
    }

    @Override
    public String setupWorld(SetupObject object) {
        SetupUtils.manager.updateGenerators();
        ConfigurationNode[] steps = object.step == null ? new ConfigurationNode[0] : object.step;
        String world = object.world;
        int type = object.type;
        String worldPath = "worlds." + object.world;
        if (!PS.get().config.contains(worldPath)) {
            PS.get().config.createSection(worldPath);
        }
        ConfigurationSection worldSection = PS.get().config.getConfigurationSection(worldPath);
        switch (type) {
            case 2: {
                if (object.id != null) {
                    String areaName = object.id + "-" + object.min + "-" + object.max;
                    String areaPath = "areas." + areaName;
                    if (!worldSection.contains(areaPath)) {
                        worldSection.createSection(areaPath);
                    }
                    ConfigurationSection areaSection = worldSection.getConfigurationSection(areaPath);
                    HashMap<String, Object> options = new HashMap<>();
                    for (ConfigurationNode step : steps) {
                        options.put(step.getConstant(), step.getValue());
                    }
                    options.put("generator.type", object.type);
                    options.put("generator.terrain", object.terrain);
                    options.put("generator.plugin", object.plotManager);
                    if ((object.setupGenerator != null) && !object.setupGenerator.equals(object.plotManager)) {
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
                GeneratorWrapper<?> gen = generators.get(object.setupGenerator);
                if ((gen != null) && gen.isFull()) {
                    object.setupGenerator = null;
                }
                break;
            }
            case 1: {
                for (ConfigurationNode step : steps) {
                    worldSection.set(step.getConstant(), step.getValue());
                }
                PS.get().config.set("worlds." + world + "." + "generator.type", object.type);
                PS.get().config.set("worlds." + world + "." + "generator.terrain", object.terrain);
                PS.get().config.set("worlds." + world + "." + "generator.plugin", object.plotManager);
                if ((object.setupGenerator != null) && !object.setupGenerator.equals(object.plotManager)) {
                    PS.get().config.set("worlds." + world + "." + "generator.init", object.setupGenerator);
                }
                GeneratorWrapper<?> gen = generators.get(object.setupGenerator);
                if ((gen != null) && gen.isFull()) {
                    object.setupGenerator = null;
                }
                break;
            }
            case 0: {
                for (ConfigurationNode step : steps) {
                    worldSection.set(step.getConstant(), step.getValue());
                }
                break;
            }
        }
        try {
            PS.get().config.save(PS.get().configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (object.setupGenerator != null) {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core")
                    .isEnabled()) {
                Bukkit.getServer()
                        .dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.setupGenerator);
                setGenerator(world, object.setupGenerator);
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world + " plugin:" + object.setupGenerator);
                setGenerator(world, object.setupGenerator);
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            WorldCreator wc = new WorldCreator(object.world);
            wc.generator(object.setupGenerator);
            wc.environment(Environment.NORMAL);
            Bukkit.createWorld(wc);
            setGenerator(world, object.setupGenerator);
        } else {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core")
                    .isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal");
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world);
                if (Bukkit.getWorld(world) != null) {
                    return world;
                }
            }
            Bukkit.createWorld(new WorldCreator(object.world).environment(World.Environment.NORMAL));
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

    @Override
    public String getGenerator(PlotArea plotworld) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        World world = Bukkit.getWorld(plotworld.worldname);
        if (world == null) {
            return null;
        }
        ChunkGenerator generator = world.getGenerator();
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        for (Entry<String, GeneratorWrapper<?>> entry : generators.entrySet()) {
            GeneratorWrapper<?> current = entry.getValue();
            if (current.equals(generator)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
