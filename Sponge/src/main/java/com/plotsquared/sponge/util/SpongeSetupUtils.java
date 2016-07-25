package com.plotsquared.sponge.util;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.sponge.generator.SpongePlotGenerator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public class SpongeSetupUtils extends SetupUtils {

    @Override
    public void updateGenerators() {
        if (!SetupUtils.generators.isEmpty()) {
            return;
        }
        SetupUtils.generators.put("PlotSquared", new SpongePlotGenerator(new HybridGen()));
        // TODO get external world generators
        Collection<WorldGeneratorModifier> wgms = Sponge.getRegistry().getAllOf(WorldGeneratorModifier.class);
        for (WorldGeneratorModifier wgm : wgms) {
            String id = wgm.getId();
            String name = wgm.getName();
            if (wgm instanceof GeneratorWrapper<?>) {
                SetupUtils.generators.put(id, (GeneratorWrapper<?>) wgm);
                SetupUtils.generators.put(name, (GeneratorWrapper<?>) wgm);
            } else {
                SpongePlotGenerator wrap = new SpongePlotGenerator(wgm);
                SetupUtils.generators.put(id, wrap);
                SetupUtils.generators.put(name, wrap);
            }
        }
    }

    @Override
    public String getGenerator(PlotArea plotArea) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        World world = SpongeUtil.getWorld(plotArea.worldname);
        if (world == null) {
            return null;
        }
        WorldGenerator generator = world.getWorldGenerator();
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public String setupWorld(SetupObject object) {
        SetupUtils.manager.updateGenerators();
        ConfigurationNode[] steps = object.step == null ? new ConfigurationNode[0] : object.step;
        String world = object.world;
        int type = object.type;
        String worldPath = "worlds." + object.world;
        if (!PS.get().worlds.contains(worldPath)) {
            PS.get().worlds.createSection(worldPath);
        }
        ConfigurationSection worldSection = PS.get().worlds.getConfigurationSection(worldPath);
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
                    if (object.setupGenerator != null && !object.setupGenerator.equals(object.plotManager)) {
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
                PS.get().worlds.set("worlds." + world + ".generator.type", object.type);
                PS.get().worlds.set("worlds." + world + ".generator.terrain", object.terrain);
                PS.get().worlds.set("worlds." + world + ".generator.plugin", object.plotManager);
                if (object.setupGenerator != null && !object.setupGenerator.equals(object.plotManager)) {
                    PS.get().worlds.set("worlds." + world + ".generator.init", object.setupGenerator);
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
            PS.get().worlds.save(PS.get().worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (object.setupGenerator != null) {
            // create world with generator
            GeneratorWrapper<?> gw = SetupUtils.generators.get(object.setupGenerator);
            WorldGeneratorModifier wgm = (WorldGeneratorModifier) gw.getPlatformGenerator();

            WorldArchetype settings = WorldArchetype.builder()
            .loadsOnStartup(true)
            .keepsSpawnLoaded(true)
            .dimension(DimensionTypes.OVERWORLD)
            .generator(GeneratorTypes.FLAT)
            .usesMapFeatures(false)
            .enabled(true)
            .generatorModifiers(wgm)
            .build("PS",object.world);
            WorldProperties properties = null;
            try {
                properties = Sponge.getServer().createWorldProperties(object.world, settings);
            } catch (IOException e) {
                e.printStackTrace();
            }
            World worldObj;
            Optional<World> world1 = Sponge.getServer().loadWorld(properties);
            if (world1.isPresent()) {
                worldObj = world1.get();
            }
        } else {
            // create vanilla world
            WorldArchetype settings = WorldArchetype.builder()
            .loadsOnStartup(true)
            .keepsSpawnLoaded(true)
            .dimension(DimensionTypes.OVERWORLD)
            .generator(GeneratorTypes.OVERWORLD)
            .usesMapFeatures(true)
            .enabled(true)
            .build("PS",object.world);
            WorldProperties properties = null;
            try {
                properties = Sponge.getServer().createWorldProperties(object.world, settings);
            } catch (IOException e) {
                e.printStackTrace();
            }
            World worldObj = Sponge.getServer().loadWorld(properties).get();
        }
        return object.world;
    }
}
