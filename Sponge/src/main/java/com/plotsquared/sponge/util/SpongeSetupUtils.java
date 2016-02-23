package com.plotsquared.sponge.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.WorldCreationSettings.Builder;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.sponge.generator.SpongePlotGenerator;

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
                generators.put(id, (GeneratorWrapper<?>) wgm);
                generators.put(name, (GeneratorWrapper<?>) wgm);
            } else {
                SpongePlotGenerator wrap = new SpongePlotGenerator(wgm);
                generators.put(id, wrap);
                generators.put(name, wrap);
            }
        }
    }
    
    @Override
    public String getGenerator(final PlotArea plotworld) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        final World world = SpongeUtil.getWorld(plotworld.worldname);
        if (world == null) {
            return null;
        }
        final WorldGenerator generator = world.getWorldGenerator();
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public String setupWorld(final SetupObject object) {
        SetupUtils.manager.updateGenerators();
        ConfigurationNode[] steps = object.step == null ? new ConfigurationNode[0] : object.step;
        final String world = object.world;
        int type = object.type;
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
            // create world with generator
            GeneratorWrapper<?> gw = generators.get(object.setupGenerator);
            WorldGeneratorModifier wgm = (WorldGeneratorModifier) gw.getPlatformGenerator();
            
            WorldCreationSettings settings = Sponge.getRegistry().createBuilder(Builder.class)
            .name(object.world)
            .loadsOnStartup(true)
            .keepsSpawnLoaded(false)
            .dimension(DimensionTypes.OVERWORLD)
            .generator(GeneratorTypes.FLAT)
            .usesMapFeatures(false)
            .enabled(true)
            .generatorModifiers(wgm)
            .build();
            WorldProperties properties = Sponge.getServer().createWorldProperties(settings).get();
            World worldObj = Sponge.getServer().loadWorld(properties).get();
        } else {
            // create vanilla world
            WorldCreationSettings settings = Sponge.getRegistry().createBuilder(Builder.class)
            .name(object.world)
            .loadsOnStartup(true)
            .keepsSpawnLoaded(false)
            .dimension(DimensionTypes.OVERWORLD)
            .generator(GeneratorTypes.OVERWORLD)
            .usesMapFeatures(true)
            .enabled(true)
            .build();
            WorldProperties properties = Sponge.getServer().createWorldProperties(settings).get();
            World worldObj = Sponge.getServer().loadWorld(properties).get();
        }
        return object.world;
    }
}
