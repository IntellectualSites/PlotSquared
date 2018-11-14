package com.github.intellectualsites.plotsquared.nukkit.util;

import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.nukkit.NukkitMain;
import com.github.intellectualsites.plotsquared.nukkit.generator.NukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.nukkit.util.block.NukkitHybridGen;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

public class NukkitSetupUtils extends SetupUtils {

    private final NukkitMain plugin;

    public NukkitSetupUtils(NukkitMain plugin) {
        this.plugin = plugin;
        Generator.addGenerator(NukkitHybridGen.class, "PlotSquared", 1);
    }

    @Override public void unload(String world, boolean save) {
        plugin.getServer().unloadLevel(plugin.getServer().getLevelByName(world), save);
    }

    @Override public void updateGenerators() {
        if (!SetupUtils.generators.isEmpty()) {
            return;
        }
        String testWorld = "CheckingPlotSquaredGenerator";
        HashMap<String, Object> map = new HashMap<>();
        map.put("world", testWorld);
        map.put("plot-generator", PlotSquared.get().IMP.getDefaultGenerator());
        NukkitPlotGenerator gen = new NukkitPlotGenerator(map);
        SetupUtils.generators.put(PlotSquared.imp().getPluginName(), gen);
    }

    @Override public String setupWorld(SetupObject object) {
        SetupUtils.manager.updateGenerators();
        ConfigurationNode[] steps = object.step == null ? new ConfigurationNode[0] : object.step;
        String world = object.world;
        int type = object.type;
        String worldPath = "worlds." + object.world;
        if (!PlotSquared.get().worlds.contains(worldPath)) {
            PlotSquared.get().worlds.createSection(worldPath);
        }
        ConfigurationSection worldSection = PlotSquared.get().worlds.getConfigurationSection(worldPath);
        switch (type) {
            case 2: {
                if (object.id != null) {
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
            case 1:
                for (ConfigurationNode step : steps) {
                    worldSection.set(step.getConstant(), step.getValue());
                }
                PlotSquared.get().worlds.set("worlds." + world + ".generator.type", object.type);
                PlotSquared.get().worlds.set("worlds." + world + ".generator.terrain", object.terrain);
                PlotSquared.get().worlds.set("worlds." + world + ".generator.plugin", object.plotManager);
                if (object.setupGenerator != null && !object.setupGenerator
                    .equals(object.plotManager)) {
                    PlotSquared.get().worlds
                        .set("worlds." + world + ".generator.init", object.setupGenerator);
                }
                GeneratorWrapper<?> gen = SetupUtils.generators.get(object.setupGenerator);
                if (gen != null && gen.isFull()) {
                    object.setupGenerator = null;
                }
                break;
            case 0:
                for (ConfigurationNode step : steps) {
                    worldSection.set(step.getConstant(), step.getValue());
                }
                break;
        }
        try {
            PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (object.setupGenerator != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("world", object.world);
            map.put("plot-generator", PlotSquared.get().IMP.getDefaultGenerator());
            if (!plugin.getServer()
                .generateLevel(object.world, object.world.hashCode(), NukkitHybridGen.class, map)) {
                plugin.getServer().loadLevel(object.world);
            }
            try {
                //                File nukkitFile = new File("nukkit.yml");
                //                YamlConfiguration nukkitYml = YamlConfiguration.loadConfiguration(nukkitFile);
                //                if (!nukkitYml.contains("worlds." + object.world + ".generator")) {
                //                    nukkitYml.set("worlds." + object.world + ".generator", object.setupGenerator);
                //                    nukkitYml.save(nukkitFile);
                //                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            if (!plugin.getServer().generateLevel(object.world, object.world.hashCode())) {
                plugin.getServer().loadLevel(object.world);
            }
        }
        return object.world;
    }

    @Override public String getGenerator(PlotArea plotArea) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        Level world = NukkitUtil.getWorld(plotArea.worldname);
        if (world == null) {
            return null;
        }
        try {
            Field field = Level.class.getDeclaredField("generatorInstance");
            field.setAccessible(true);
            Generator generator = (Generator) field.get(world);
            if (!(generator instanceof NukkitPlotGenerator)) {
                return null;
            }
            for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
                GeneratorWrapper<?> current = entry.getValue();
                if (current.equals(generator)) {
                    return entry.getKey();
                }
            }
            return PlotSquared.imp().getPluginName();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
