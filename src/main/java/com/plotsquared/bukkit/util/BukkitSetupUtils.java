package com.plotsquared.bukkit.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.sponge.generator.AugmentedPopulator;

public class BukkitSetupUtils extends SetupUtils {
    
    @Override
    public void updateGenerators() {
        if (SetupUtils.generators.size() > 0) {
            return;
        }
        final String testWorld = "CheckingPlotSquaredGenerator";
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.isEnabled()) {
                final ChunkGenerator generator = plugin.getDefaultWorldGenerator(testWorld, "");
                if (generator != null) {
                    PS.get().removePlotAreas(testWorld);
                    final String name = plugin.getDescription().getName();
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
    public String setupWorld(final SetupObject object) {
        SetupUtils.manager.updateGenerators();
        //
        ConfigurationNode[] steps = object.step;
        final String world = object.world;
        int type = object.type; // TODO type = 2
        String worldPath = "worlds." + object.world;
        if (!PS.get().config.contains(worldPath)) {
            PS.get().config.createSection(worldPath);
        }
        ConfigurationSection worldSection = PS.get().config.getConfigurationSection(worldPath);
        switch (type) {
            case 2: {
                if (object.id != null) {
                    String areaname = object.id + "-" + object.min + "-" + object.max;
                    String areaPath = "areas." + areaname;
                    if (!worldSection.contains(areaPath)) {
                        worldSection.createSection(areaPath);
                    }
                    ConfigurationSection areaSection = worldSection.getConfigurationSection(areaPath);
                    HashMap<String, Object> options = new HashMap<>();
                    for (final ConfigurationNode step : steps) {
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
                for (final ConfigurationNode step : steps) {
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
                for (final ConfigurationNode step : steps) {
                    worldSection.set(step.getConstant(), step.getValue());
                }
                break;
            }
        }
        try {
            PS.get().config.save(PS.get().configFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (object.setupGenerator != null) {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.setupGenerator);
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
            final WorldCreator wc = new WorldCreator(object.world);
            wc.generator(object.setupGenerator);
            wc.environment(Environment.NORMAL);
            Bukkit.createWorld(wc);
            setGenerator(world, object.setupGenerator);
        } else {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
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
    
    public void setGenerator(final String world, final String generator) {
        if ((Bukkit.getWorlds().size() == 0) || !Bukkit.getWorlds().get(0).getName().equals(world)) {
            return;
        }
        final File file = new File("bukkit.yml").getAbsoluteFile();
        final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set("worlds." + world + ".generator", generator);
        try {
            yml.save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getGenerator(final PlotArea plotworld) {
        if (SetupUtils.generators.size() == 0) {
            updateGenerators();
        }
        final World world = Bukkit.getWorld(plotworld.worldname);
        if (world == null) {
            return null;
        }
        final ChunkGenerator generator = world.getGenerator();
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        for (final Entry<String, GeneratorWrapper<?>> entry : generators.entrySet()) {
            GeneratorWrapper<?> current = entry.getValue();
            if (current.equals(generator)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    @Override
    public void removePopulator(final String world, final PlotCluster cluster) {
        AugmentedPopulator.removePopulator(world, cluster);
    }
}
