////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.plotsquared.bukkit.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.*;
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
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BukkitPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {
    
    private final PlotChunk<Chunk> chunkSetter;
    private final PseudoRandom random = new PseudoRandom();
    private final IndependentPlotGenerator plotGenerator;
    private final List<BlockPopulator> populators = new ArrayList<>();
    private boolean loaded = false;
    private ChunkGenerator platformGenerator;
    private boolean full;

    public BukkitPlotGenerator(IndependentPlotGenerator generator) {
        this.plotGenerator = generator;
        this.platformGenerator = this;
        populators.add(new BlockPopulator() {
            @Override
            public void populate(World world, Random r, Chunk c) {
                if (!(chunkSetter instanceof GenChunk)) {
                    PS.debug("Current PlotChunk is not relevant to population?");
                    PS.stacktrace();
                    return;
                }
                GenChunk result = (GenChunk) chunkSetter;
                if (result.result_data != null) {
                    for (int i = 0; i < result.result_data.length; i++) {
                        byte[] section = result.result_data[i];
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
            }
        });
        chunkSetter = new GenChunk(null, null);
        this.full = true;
        MainUtil.initCache();
    }
    
    public BukkitPlotGenerator(final String world, final ChunkGenerator cg) {
        if (cg instanceof BukkitPlotGenerator) {
            throw new IllegalArgumentException("ChunkGenerator: " + cg.getClass().getName() + " is already a BukkitPlotGenerator!");
        }
        this.full = false;
        PS.get().debug("BukkitPlotGenerator does not fully support: " + cg);
        platformGenerator = cg;
        plotGenerator = new IndependentPlotGenerator() {
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
            public void generateChunk(final PlotChunk<?> result, final PlotArea settings, final PseudoRandom random) {
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
                }
                catch (Throwable e) {}
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
        chunkSetter = new GenChunk(null, SetQueue.IMP.new ChunkWrapper(world, 0, 0));
        if (cg != null) {
            populators.addAll(cg.getDefaultPopulators(BukkitUtil.getWorld(world)));
        }
        MainUtil.initCache();
    }
    
    @Override
    public void augment(PlotArea area) {
        BukkitAugmentedGenerator.get(BukkitUtil.getWorld(area.worldname));
    }
    
    @Override
    public boolean isFull() {
        return full;
    }
    
    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return plotGenerator;
    }
    
    @Override
    public ChunkGenerator getPlatformGenerator() {
        return platformGenerator;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        try {
            if (!loaded) {
                final String name = world.getName();
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
                loaded = true;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return populators;
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid grid) {
        if (!(chunkSetter instanceof GenChunk)) {
            PS.debug("Current PlotChunk is not relevant to generation?");
            PS.stacktrace();
            return null;
        }
        GenChunk result = (GenChunk) chunkSetter;
        // Set the chunk location
        result.setChunkWrapper(SetQueue.IMP.new ChunkWrapper(world.getName(), cx, cz));
        // Set the result data
        result.cd = createChunkData(world);
        result.grid = grid;
        // Catch any exceptions (as exceptions usually thrown 
        try {
            // Fill the result data if necessary
            if (platformGenerator != this) {
                return platformGenerator.generateChunkData(world, random, cx, cz, grid);
            } else {
                generate(world, cx, cz, result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Return the result data
        return result.cd;
    }
    
    public void generate(World world, int cx, int cz, GenChunk result) {
        // Load if improperly loaded
        if (!loaded) {
            final String name = world.getName();
            PS.get().loadWorld(name, this);
            loaded = true;
        }
        // Set random seed
        this.random.state = (cx << 16) | (cz & 0xFFFF);
        // Process the chunk
        result.modified = false;
        ChunkManager.preProcessChunk(result);
        if (result.modified) {
            return;
        }
        PlotArea area = PS.get().getPlotArea(world.getName(), null);
        plotGenerator.generateChunk(chunkSetter, area, this.random);
        ChunkManager.postProcessChunk(result);
        return;
    }
    
    @Override
    public short[][] generateExtBlockSections(final World world, final Random r, final int cx, final int cz, final BiomeGrid grid) {
        if (!(chunkSetter instanceof GenChunk)) {
            PS.stacktrace();
            return new short[16][];
        }
        GenChunk result = (GenChunk) chunkSetter;
        // Set the chunk location
        result.setChunkWrapper(SetQueue.IMP.new ChunkWrapper(world.getName(), cx, cz));
        // Set the result data
        result.result = new short[16][];
        result.result_data = new byte[16][];
        result.grid = grid;
        // Catch any exceptions (as exceptions usually thrown 
        try {
            // Fill the result data
            if (platformGenerator != this) {
                return platformGenerator.generateExtBlockSections(world, r, cx, cz, grid);
            } else {
                generate(world, cx, cz, result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Return the result data
        return result.result;
    }
    
    /**
     * Allow spawning everywhere
     */
    @Override
    public boolean canSpawn(final World world, final int x, final int z) {
        return true;
    }
    
    @Override
    public String toString() {
        if (platformGenerator == this) {
            return "" + plotGenerator;
        }
        return platformGenerator == null ? "null" : platformGenerator.getClass().getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return (toString().equals(obj.toString()) || toString().equals(obj.getClass().getName()));
    }
}
