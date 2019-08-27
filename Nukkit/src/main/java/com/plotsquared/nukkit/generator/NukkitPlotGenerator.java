package com.plotsquared.nukkit.generator;

import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;
import com.plotsquared.nukkit.util.NukkitUtil;
import com.plotsquared.nukkit.util.block.NukkitWrappedChunk;

import java.util.Map;

public class NukkitPlotGenerator extends Generator implements GeneratorWrapper<Generator> {

    protected final PseudoRandom random = new PseudoRandom();
    protected final IndependentPlotGenerator plotGenerator;
    protected final Generator platformGenerator;
    protected final boolean full;
    protected final String world;
    protected final Map<String, Object> settings;
    protected boolean loaded = false;
    protected cn.nukkit.level.ChunkManager chunkManager;
    protected final NukkitWrappedChunk chunkSetter;

    public NukkitPlotGenerator(Map<String, Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("options may not be null!");
        }
        this.settings = map;
        MainUtil.initCache();
        this.world = map.get("world").toString();
        if (map.containsKey("generator")) {
            final Generator cg = (Generator) map.get("generator");
            if (cg instanceof NukkitPlotGenerator) {
                throw new IllegalArgumentException("Generator: " + cg.getClass().getName() + " is already a NukkitPlotGenerator!");
            }
            this.full = false;
            PS.debug("NukkitPlotGenerator does not fully support: " + cg);
            this.platformGenerator = cg;
            this.plotGenerator = new IndependentPlotGenerator() {
                @Override
                public void processSetup(SetupObject setup) {}

                @Override
                public void initialize(PlotArea area) {}

                @Override
                public PlotManager getNewPlotManager() {
                    return PS.get().IMP.getDefaultGenerator().getNewPlotManager();
                }

                @Override
                public String getName() {
                    return cg.getClass().getName();
                }

                @Override
                public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
                    return PS.get().IMP.getDefaultGenerator().getNewPlotArea(world, id, min, max);
                }

                @Override
                public void generateChunk(final ScopedLocalBlockQueue result, PlotArea settings, PseudoRandom random) {
                    Location min = result.getMin();
                    int cx = min.getX() >> 4;
                    int cz = min.getZ() >> 4;
                    cg.generateChunk(cx, cz);
                    cg.populateChunk(cx, cz);
                }
            };
            chunkSetter = new NukkitWrappedChunk(world, null);
            MainUtil.initCache();
        } else {
            this.plotGenerator = (IndependentPlotGenerator) map.get("plot-generator");
            this.platformGenerator = this;
            this.full = true;
            chunkSetter = new NukkitWrappedChunk(world, null);
        }
    }

    @Override
    public void augment(PlotArea area) {
        NukkitAugmentedGenerator.get(NukkitUtil.getWorld(area.worldname));
    }
    
    @Override
    public boolean isFull() {
        return this.full;
    }
    
    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return this.plotGenerator;
    }
    
    @Override
    public Generator getPlatformGenerator() {
        return this.platformGenerator;
    }

    @Override
    public String toString() {
        if (this.platformGenerator == this) {
            return this.plotGenerator.getName();
        }
        if (this.platformGenerator == null) {
            return "null";
        } else {
            return this.platformGenerator.getClass().getName();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return toString().equals(obj.toString()) || toString().equals(obj.getClass().getName());
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public void init(cn.nukkit.level.ChunkManager chunkManager, NukkitRandom nukkitRandom) {
        if (this.chunkManager == null) {
            PS.get().loadWorld(world, this);
        }
        this.chunkManager = chunkManager;
        if (getPlatformGenerator() != this) {
            getPlatformGenerator().init(chunkManager, nukkitRandom);
        }
    }

    @Override
    public void generateChunk(int cx, int cz) {
        if (getPlatformGenerator() != this) {
            getPlatformGenerator().generateChunk(cx, cz);
        } else {
            BaseFullChunk chunk = this.chunkManager.getChunk(cx, cz);
            // Load if improperly loaded
            if (!this.loaded) {
                PS.get().loadWorld(world, this);
                this.loaded = true;
            }
            chunkSetter.init(chunk);
            // Set random seed
            this.random.state = cx << 16 | cz & 0xFFFF;
            // Process the chunk
            if (ChunkManager.preProcessChunk(chunkSetter)) {
                return;
            }
            PlotArea area = PS.get().getPlotArea(world, null);
            try {
                this.plotGenerator.generateChunk(this.chunkSetter, area, this.random);
            } catch (Throwable e) {
                // Recover from generator error
                e.printStackTrace();
            }
            ChunkManager.postProcessChunk(chunkSetter);
        }
    }

    @Override
    public void populateChunk(int x, int z) {
        if (getPlatformGenerator() != this) {
            getPlatformGenerator().populateChunk(x, z);
        }  // No populating

    }

    @Override
    public Map<String, Object> getSettings() {
        return settings;
    }

    @Override
    public String getName() {
        return plotGenerator.getName();
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(0, 61, 0);
    }

    @Override
    public cn.nukkit.level.ChunkManager getChunkManager() {
        return chunkManager;
    }
}
