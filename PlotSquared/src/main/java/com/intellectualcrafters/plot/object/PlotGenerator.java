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
package com.intellectualcrafters.plot.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.listeners.WorldEvents;
import com.intellectualcrafters.plot.util.ChunkManager;

public abstract class PlotGenerator extends ChunkGenerator {

    public static short[][][] CACHE_I = null;
    public static short[][][] CACHE_J = null;
    public int X;
    public int Z;
    private boolean loaded = false;
    private short[][] result;
    private PseudoRandom random = new PseudoRandom();
    
    public PlotGenerator(String world) {
        WorldEvents.lastWorld = world;
        initCache();
    }
    
    public void initCache() {
        if (CACHE_I == null) {
            CACHE_I = new short[256][16][16];
            CACHE_J = new short[256][16][16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        short i = (short) (y >> 4);
                        short j = (short) (((y & 0xF) << 8) | (z << 4) | x);
                        CACHE_I[y][x][z] = i;
                        CACHE_J[y][x][z] = j;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        try {
            if (!loaded) {
                PS.get().loadWorld(WorldEvents.getName(world), this);
                PlotWorld plotworld = PS.get().getPlotWorld(WorldEvents.getName(world));
                if (!plotworld.MOB_SPAWNING) {
                    if (!plotworld.SPAWN_EGGS) {
                        world.setSpawnFlags(false, false);
                    }
                    world.setAmbientSpawnLimit(0);
                    world.setAnimalSpawnLimit(0);
                    world.setMonsterSpawnLimit(0);
                    world.setWaterAnimalSpawnLimit(0);
                }
                else {
                    world.setSpawnFlags(true, true);
                    world.setAmbientSpawnLimit(-1);
                    world.setAnimalSpawnLimit(-1);
                    world.setMonsterSpawnLimit(-1);
                    world.setWaterAnimalSpawnLimit(-1);
                }
                loaded = true;
                return (List<BlockPopulator>)(List<?>) getPopulators(WorldEvents.getName(world));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<BlockPopulator>();
    }
    
    @Override
    public short[][] generateExtBlockSections(World world, Random r, int cx, int cz, BiomeGrid biomes) {
        try {
            if (!loaded) {
                PS.get().loadWorld(WorldEvents.getName(world), this);
                loaded = true;
            }
            final int prime = 13;
            int h = 1;
            h = (prime * h) + cx;
            h = (prime * h) + cz;
            this.random.state = h;
            this.result = new short[16][];
            this.X = cx << 4;
            this.Z = cz << 4;
            if (ChunkManager.FORCE_PASTE) {
                PlotWorld plotworld = PS.get().getPlotWorld(world.getName());
                Biome biome = Biome.valueOf(plotworld.PLOT_BIOME);
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        if (biomes != null) {
                            biomes.setBiome(x, z, biome);
                        }
                        final PlotLoc loc = new PlotLoc((X + x), (Z + z));
                        final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                        for (final Entry<Short, Short> entry : blocks.entrySet()) {
                            setBlock(x, entry.getKey(), z, entry.getValue());
                        }
                    }
                }
                return this.result;
            }
            generateChunk(world, ChunkManager.CURRENT_PLOT_CLEAR, random, cx, cz, biomes);
            if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                PlotLoc loc;
                for (Entry<PlotLoc, HashMap<Short, Short>> entry : ChunkManager.GENERATE_BLOCKS.entrySet()) {
                    for (Entry<Short, Short> entry2 : entry.getValue().entrySet()) {
                        loc = entry.getKey();
                        int xx = loc.x - X;
                        int zz = loc.z - Z;
                        if (xx >= 0 && xx < 16) {
                        	if (zz >= 0 && zz < 16) {
                        		setBlock(xx, entry2.getKey(), zz, entry2.getValue());
                        	}
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public void setBlock(final int x, final int y, final int z, final short blkid) {
        if (result[CACHE_I[y][x][z]] == null) {
            result[CACHE_I[y][x][z]] = new short[4096];
        }
        result[CACHE_I[y][x][z]][CACHE_J[y][x][z]] = blkid;
    }
    
    public void setBlock(final int x, final int y, final int z, final short[] blkid) {
        if (blkid.length == 1) {
            setBlock(x, y, z, blkid[0]);
        }
        short id = blkid[random.random(blkid.length)];
        if (result[CACHE_I[y][x][z]] == null) {
            result[CACHE_I[y][x][z]] = new short[4096];
        }
        result[CACHE_I[y][x][z]][CACHE_J[y][x][z]] = id;
    }
    
    /**
     * check if a region contains a location. (x, z) must be between [0,15], [0,15]
     * @param plot
     * @param x
     * @param z
     * @return
     */
    public boolean contains(final RegionWrapper plot, final int x, final int z) {
        int xx = X + x;
        int zz = Z + z;
        return ((xx >= plot.minX) && (xx <= plot.maxX) && (zz >= plot.minZ) && (zz <= plot.maxZ));
    }
    
    /**
     * Allow spawning everywhere
     */
    @Override
    public boolean canSpawn(final World world, final int x, final int z) {
        return true;
    }
    
    /**
     * <b>random</b> is an optimized random number generator.<br> 
     *  - Change the state to have the same chunk random each time it generates<br>
     *  <b>requiredRegion</b> If a plot is being regenerated, you are only required to generate content in this area<br>
     *   - use the contains(RegionWrapper, x, z) method to check if the region contains a location<br>
     *   - You can ignore this if you do not want to further optimize your generator<br>
     *   - will be null if no restrictions are set<br>
     *   <b>result</b> is the standard 2D block data array used for generation<br>
     *   <b>biomes</b> is the standard BiomeGrid used for generation
     * 
     * @param world
     * @param random
     * @param cx
     * @param cz
     * @param requiredRegion
     * @param biomes
     * @param result
     * @return
     */
    public abstract void generateChunk(final World world, RegionWrapper requiredRegion, final PseudoRandom random, final int cx, final int cz, final BiomeGrid biomes);
    
    public abstract List<PlotPopulator> getPopulators(String world);
    
    /**
     * This is called when the generator is initialized. 
     * You don't need to do anything with it necessarily.
     * @param plotworld
     */
    public abstract void init(PlotWorld plotworld);
    
    /**
     * Return a new instance of the PlotWorld for a world 
     * @param world
     * @return
     */
    public abstract PlotWorld getNewPlotWorld(final String world);

    /**
     * Get the PlotManager class for this generator
     * @return
     */
    public abstract PlotManager getPlotManager();
    
    /**
     * If you need to do anything fancy for /plot setup<br>
     *  - Otherwise it will just use the PlotWorld configuration<br>
     * Feel free to extend BukkitSetupUtils and customize world creation
     * @param object
     */
    public void processSetup(SetupObject object) {
    }
}
