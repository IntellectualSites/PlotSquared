package com.plotsquared.bukkit.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.object.worlds.SingleWorldGenerator;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.block.GlobalBlockQueue;
import com.intellectualcrafters.plot.util.block.LocalBlockQueue;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.block.GenChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class BukkitPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {
    
    private final GenChunk chunkSetter;
    private final PseudoRandom random = new PseudoRandom();
    private final IndependentPlotGenerator plotGenerator;
    private List<BlockPopulator> populators;
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private final HashMap<ChunkLoc, byte[][]> dataMap = new HashMap<>();
    private boolean loaded = false;

    public BukkitPlotGenerator(IndependentPlotGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator may not be null!");
        }
        this.plotGenerator = generator;
        this.platformGenerator = this;
        populators = new ArrayList<>();
        this.populators.add(new BlockPopulator() {

            private LocalBlockQueue queue;

            @Override
            public void populate(World world, Random r, Chunk c) {
                if (queue == null) {
                    queue = GlobalBlockQueue.IMP.getNewQueue(world.getName(), false);
                }
                byte[][] resultData;
                if (dataMap.isEmpty()) {
                    resultData = null;
                } else {
                    resultData = dataMap.remove(new ChunkLoc(c.getX(), c.getZ()));
                }
                if (resultData == null) {
                    GenChunk result = BukkitPlotGenerator.this.chunkSetter;
                    // Set the chunk location
                    result.setChunk(c);
                    // Set the result data
                    result.result = new short[16][];
                    result.result_data = new byte[16][];
                    result.grid = null;
                    result.cd = null;
                    // Catch any exceptions (as exceptions usually thrown)
                    generate(world, c.getX(), c.getZ(), result);
                    resultData = result.result_data;
                }
                if (resultData != null) {
                    for (int i = 0; i < resultData.length; i++) {
                        byte[] section = resultData[i];
                        if (section == null) {
                            continue;
                        }
                        for (int j = 0; j < section.length; j++) {
                            int x = MainUtil.x_loc[i][j];
                            int y = MainUtil.y_loc[i][j];
                            int z = MainUtil.z_loc[i][j];
                            c.getBlock(x, y, z).setData(section[j]);
                        }
                    }
                }
                BukkitPlotGenerator.this.random.state = c.getX() << 16 | c.getZ() & 0xFFFF;
                PlotArea area = PS.get().getPlotArea(world.getName(), null);
                ChunkWrapper wrap = new ChunkWrapper(area.worldname, c.getX(), c.getZ());
                ScopedLocalBlockQueue chunk = queue.getForChunk(wrap.x, wrap.z);
                if (BukkitPlotGenerator.this.plotGenerator.populateChunk(chunk, area, BukkitPlotGenerator.this.random)) {
                    queue.flush();
                }
            }
        });
        this.chunkSetter = new GenChunk(null, null);
        this.full = true;
        MainUtil.initCache();
    }
    
    public BukkitPlotGenerator(final String world, final ChunkGenerator cg) {
        if (cg instanceof BukkitPlotGenerator) {
            throw new IllegalArgumentException("ChunkGenerator: " + cg.getClass().getName() + " is already a BukkitPlotGenerator!");
        }
        this.full = false;
        PS.debug("BukkitPlotGenerator does not fully support: " + cg);
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
                World w = BukkitUtil.getWorld(world);
                Location min = result.getMin();
                int cx = min.getX() >> 4;
                int cz = min.getZ() >> 4;
                Random r = new Random(MathMan.pair((short) cx, (short) cz));
                BiomeGrid grid = new BiomeGrid() {
                    @Override
                    public void setBiome(int x, int z, Biome biome) {
                        result.setBiome(x, z, biome.name());
                    }
                    
                    @Override
                    public Biome getBiome(int arg0, int arg1) {
                        return Biome.FOREST;
                    }
                };
                try {
                    // ChunkData will spill a bit
                    ChunkData data = cg.generateChunkData(w, r, cx, cz, grid);
                    if (data != null) {
                        return;
                    }
                } catch (Throwable ignored) {}
                // Populator spillage
                short[][] tmp = cg.generateExtBlockSections(w, r, cx, cz, grid);
                if (tmp != null) {
                    for (int i = 0; i < tmp.length; i++) {
                        short[] section = tmp[i];
                        if (section == null) {
                            if (i < 7) {
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        for (int y = i << 4; y < (i << 4) + 16; y++) {
                                            result.setBlock(x, y, z, (short) 0, (byte) 0);
                                        }
                                    }
                                }
                            }
                            continue;
                        }
                        for (int j = 0; j < section.length; j++) {
                            int x = MainUtil.x_loc[i][j];
                            int y = MainUtil.y_loc[i][j];
                            int z = MainUtil.z_loc[i][j];
                            result.setBlock(x, y, z, section[j], (byte) 0);
                        }
                    }
                }
                for (BlockPopulator populator : cg.getDefaultPopulators(w)) {
                    populator.populate(w, r, w.getChunkAt(cx, cz));
                }
            }
        };
        this.chunkSetter = new GenChunk(null, new ChunkWrapper(world, 0, 0));
        MainUtil.initCache();
    }
    
    @Override
    public void augment(PlotArea area) {
        BukkitAugmentedGenerator.get(BukkitUtil.getWorld(area.worldname));
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
    public ChunkGenerator getPlatformGenerator() {
        return this.platformGenerator;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        try {
            if (!this.loaded) {
                String name = world.getName();
                PS.get().loadWorld(name, this);
                Set<PlotArea> areas = PS.get().getPlotAreas(name);
                if (!areas.isEmpty()) {
                    PlotArea area = areas.iterator().next();
                    if (!area.MOB_SPAWNING) {
                        if (!area.SPAWN_EGGS) {
                            world.setSpawnFlags(false, false);
                        }
                        world.setAmbientSpawnLimit(0);
                        world.setAnimalSpawnLimit(0);
                        world.setMonsterSpawnLimit(0);
                        world.setWaterAnimalSpawnLimit(0);
                    } else {
                        world.setSpawnFlags(true, true);
                        world.setAmbientSpawnLimit(-1);
                        world.setAnimalSpawnLimit(-1);
                        world.setMonsterSpawnLimit(-1);
                        world.setWaterAnimalSpawnLimit(-1);
                    }
                }
                this.loaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<BlockPopulator> toAdd = new ArrayList<>();
        List<BlockPopulator> existing = world.getPopulators();
        if (populators == null && platformGenerator != null) {
            populators = new ArrayList<>(platformGenerator.getDefaultPopulators(world));
        }
        for (BlockPopulator populator : this.populators) {
            if (!existing.contains(populator)) {
                toAdd.add(populator);
            }
        }
        return toAdd;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid grid) {
        GenChunk result = this.chunkSetter;
        if (this.getPlotGenerator() instanceof SingleWorldGenerator) {
            if (result.cd != null) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        grid.setBiome(x, z, Biome.PLAINS);
                    }
                }
                return result.cd;
            }
        }
        // Set the chunk location
        result.setChunk(new ChunkWrapper(world.getName(), cx, cz));
        // Set the result data
        result.cd = createChunkData(world);
        result.grid = grid;
        result.result = null;
        result.result_data = null;
        // Catch any exceptions (as exceptions usually thrown)
        try {
            // Fill the result data if necessary
            if (this.platformGenerator != this) {
                return this.platformGenerator.generateChunkData(world, random, cx, cz, grid);
            } else {
                generate(world, cx, cz, result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Return the result data
        return result.cd;
    }

    public void generate(World world, int cx, int cz, ScopedLocalBlockQueue result) {
        // Load if improperly loaded
        if (!this.loaded) {
            String name = world.getName();
            PS.get().loadWorld(name, this);
            this.loaded = true;
        }
        // Set random seed
        this.random.state = cx << 16 | cz & 0xFFFF;
        // Process the chunk
        if (ChunkManager.preProcessChunk(result)) {
            return;
        }
        PlotArea area = PS.get().getPlotArea(world.getName(), null);
        try {
            this.plotGenerator.generateChunk(this.chunkSetter, area, this.random);
        } catch (Throwable e) {
            // Recover from generator error
            e.printStackTrace();
        }
        ChunkManager.postProcessChunk(result);
    }
    
    @Override
    public short[][] generateExtBlockSections(World world, Random r, int cx, int cz, BiomeGrid grid) {
        GenChunk result = this.chunkSetter;
        // Set the chunk location
        result.setChunk(new ChunkWrapper(world.getName(), cx, cz));
        // Set the result data
        result.result = new short[16][];
        result.result_data = new byte[16][];
        result.grid = grid;
        result.cd = null;
        // Catch any exceptions (as exceptions usually thrown)
        try {
            // Fill the result data
            if (this.platformGenerator != this) {
                return this.platformGenerator.generateExtBlockSections(world, r, cx, cz, grid);
            } else {
                generate(world, cx, cz, result);
                for (int i = 0; i < result.result_data.length; i++) {
                    if (result.result_data[i] != null) {
                        this.dataMap.put(new ChunkLoc(cx, cz), result.result_data);
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Return the result data
        return result.result;
    }
    
    /**
     * Allow spawning everywhere.
     * @param world Ignored
     * @param x Ignored
     * @param z Ignored
     * @return always true
     */
    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
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
}
