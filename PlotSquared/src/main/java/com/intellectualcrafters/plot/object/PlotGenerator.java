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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.util.ChunkManager;

public abstract class PlotGenerator extends ChunkGenerator {
    
    private short[][] result;
    private int X;
    private int Z;
    private PseudoRandom random = new PseudoRandom();

    @SuppressWarnings("unchecked")
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        PlotSquared.loadWorld(world.getName(), this);
        PlotWorld plotworld = PlotSquared.getPlotWorld(world.getName());
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
        return (List<BlockPopulator>)(List<?>) getPopulators(world.getName());
    }
    
    @Override
    public short[][] generateExtBlockSections(final World world, final Random r, final int cx, final int cz, final BiomeGrid biomes) {
        final int prime = 13;
        int h = 1;
        h = (prime * h) + cx;
        h = (prime * h) + cz;
        this.random.state = h;
        this.result = new short[256 / 16][];
        PlotWorld plotworld = PlotSquared.getPlotWorld(world.getName());
        Biome biome = Biome.valueOf(plotworld.PLOT_BIOME);
        if (ChunkManager.FORCE_PASTE) {
            X = cx << 4;
            Z = cz << 4;
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    if (biomes != null) {
                        biomes.setBiome(x, z, biome);
                    }
                    final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                    final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                    for (final Entry<Short, Short> entry : blocks.entrySet()) {
                        setBlock(x, entry.getKey(), z, entry.getValue());
                    }
                }
            }
            return this.result;
        }
        this.result = generateChunk(world, ChunkManager.CURRENT_PLOT_CLEAR, random, cx, cz, biomes, result);
        if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
            PlotLoc loc;
            for (Entry<PlotLoc, HashMap<Short, Short>> entry : ChunkManager.GENERATE_BLOCKS.entrySet()) {
                for (Entry<Short, Short> entry2 : entry.getValue().entrySet()) {
                    loc = entry.getKey();
                    setBlock(loc.x, entry2.getKey(), loc.z, entry2.getValue());
                }
            }
        }
        return result;
    }
    
    private void setBlock(final int x, final int y, final int z, final short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
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
     * <b>random</b> is a optimized random number generator.<br> 
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
    public abstract short[][] generateChunk(final World world, RegionWrapper requiredRegion, final PseudoRandom random, final int cx, final int cz, final BiomeGrid biomes, final short[][] result);
    
    public abstract List<PlotPopulator> getPopulators(String world);
    
    public abstract void init(PlotWorld plotworld);
    
    public abstract PlotWorld getNewPlotWorld(final String world);

    public abstract PlotManager getPlotManager();

}
