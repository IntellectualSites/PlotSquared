package com.plotsquared.bukkit.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.block.GenChunk;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BukkitPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {
    
    private final PlotChunk<Chunk> chunkSetter;
    private final PseudoRandom random = new PseudoRandom();
    private final IndependentPlotGenerator plotGenerator;
    private final List<BlockPopulator> populators = new ArrayList<>();
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private final HashMap<ChunkLoc, byte[][]> dataMap = new HashMap<>();
    private boolean loaded = false;

    public BukkitPlotGenerator(IndependentPlotGenerator generator) {
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.populators.add(new BlockPopulator() {
            @Override
            public void populate(World world, Random r, Chunk c) {
                ChunkLoc loc = new ChunkLoc(c.getX(), c.getZ());
                byte[][] resultData;
                if (!BukkitPlotGenerator.this.dataMap.containsKey(loc)) {
                    GenChunk result = (GenChunk) BukkitPlotGenerator.this.chunkSetter;
                    // Set the chunk location
                    result.setChunkWrapper(SetQueue.IMP.new ChunkWrapper(world.getName(), loc.x, loc.z));
                    // Set the result data
                    result.result = new short[16][];
                    result.result_data = new byte[16][];
                    result.grid = null;
                    result.cd = null;
                    // Catch any exceptions (as exceptions usually thrown)
                    generate(world, loc.x, loc.z, result);
                    resultData = result.result_data;
                } else {
                    resultData = BukkitPlotGenerator.this.dataMap.remove(loc);
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
                SetQueue.ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(area.worldname, c.getX(), c.getZ());
                PlotChunk<?> chunk = SetQueue.IMP.queue.getChunk(wrap);
                if (BukkitPlotGenerator.this.plotGenerator.populateChunk(chunk, area, BukkitPlotGenerator.this.random)) {
                    chunk.addToQueue();
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
                return new HybridGen().getNewPlotManager();
            }
            
            @Override
            public String getName() {
                return cg.getClass().getName();
            }
            
            @Override
            public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
                return new HybridGen().getNewPlotArea(world, id, min, max);
            }
            
            @Override
            public void generateChunk(final PlotChunk<?> result, PlotArea settings, PseudoRandom random) {
                World w = BukkitUtil.getWorld(world);
                Random r = new Random(result.getChunkWrapper().hashCode());
                BiomeGrid grid = new BiomeGrid() {
                    @Override
                    public void setBiome(int x, int z, Biome biome) {
                        result.setBiome(x, z, biome.ordinal());
                    }
                    
                    @Override
                    public Biome getBiome(int arg0, int arg1) {
                        return Biome.FOREST;
                    }
                };
                try {
                    // ChunkData will spill a bit
                    ChunkData data = cg.generateChunkData(w, r, result.getX(), result.getZ(), grid);
                    if (data != null) {
                        return;
                    }
                } catch (Throwable e) {
                    //ignored
                }
                // Populator spillage
                short[][] tmp = cg.generateExtBlockSections(w, r, result.getX(), result.getZ(), grid);
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
                    populator.populate(w, r, (Chunk) result.getChunk());
                }
            }
        };
        this.chunkSetter = new GenChunk(null, SetQueue.IMP.new ChunkWrapper(world, 0, 0));
        if (cg != null) {
            this.populators.addAll(cg.getDefaultPopulators(BukkitUtil.getWorld(world)));
        }
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
        ArrayList<BlockPopulator> toAdd = new ArrayList<BlockPopulator>();
        List<BlockPopulator> existing = world.getPopulators();
        for (BlockPopulator populator : this.populators) {
            if (!existing.contains(populator)) {
                toAdd.add(populator);
            }
        }
        return toAdd;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid grid) {
        GenChunk result = (GenChunk) this.chunkSetter;
        // Set the chunk location
        result.setChunkWrapper(SetQueue.IMP.new ChunkWrapper(world.getName(), cx, cz));
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

    public void generate(World world, int cx, int cz, PlotChunk<?> result) {
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
        this.plotGenerator.generateChunk(this.chunkSetter, area, this.random);
        ChunkManager.postProcessChunk(result);
    }
    
    @Override
    public short[][] generateExtBlockSections(World world, Random r, int cx, int cz, BiomeGrid grid) {
        GenChunk result = (GenChunk) this.chunkSetter;
        // Set the chunk location
        result.setChunkWrapper(SetQueue.IMP.new ChunkWrapper(world.getName(), cx, cz));
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
                this.dataMap.put(new ChunkLoc(cx, cz), result.result_data);

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
