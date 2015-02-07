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

package com.intellectualcrafters.plot.generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;

/**
 * The default generator is very messy, as we have decided to try externalize all calculations from within the loop. -
 * You will see a lot of slower implementations have a single for loop. - This is perfectly fine to do, it will just
 * mean world generation may take somewhat longer
 *
 * @author Citymonstret
 * @author Empire92
 */
public class HybridGen extends PlotGenerator {
    /**
     * Set to static to re-use the same managet for all Default World Generators
     */
    private static PlotManager manager = null;
    /**
     * plotworld object
     */
    public HybridPlotWorld plotworld = null;
    
    /**
     * Some generator specific variables (implementation dependent)
     */
    final int plotsize;
    final int pathsize;
    final short wall;
    final short wallfilling;
    final short roadblock;
    final int size;
    final Biome biome;
    final int roadheight;
    final int wallheight;
    final int plotheight;
    final short[] plotfloors;
    final short[] filling;
    final short pathWidthLower;
    final short pathWidthUpper;
    boolean doState = false;
    int maxY;
    /**
     * result object is returned for each generated chunk, do stuff to it
     */
    short[][] result;
    /**
     * Faster sudo-random number generator than java.util.random
     */
    private long state = 13;

    /**
     * Initialize variables, and create plotworld object used in calculations
     */
    public HybridGen(final String world) {
        super(world);
        if (this.plotworld == null) {
            this.plotworld = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        }
        this.plotsize = this.plotworld.PLOT_WIDTH;

        this.pathsize = this.plotworld.ROAD_WIDTH;

        this.roadblock = this.plotworld.ROAD_BLOCK.id;

        this.wallfilling = this.plotworld.WALL_FILLING.id;
        this.size = this.pathsize + this.plotsize;
        this.wall = this.plotworld.WALL_BLOCK.id;

        this.plotfloors = new short[this.plotworld.TOP_BLOCK.length];
        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++) {
            this.plotfloors[i] = this.plotworld.TOP_BLOCK[i].id;
        }
        
        this.filling = new short[this.plotworld.MAIN_BLOCK.length];
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++) {
            this.filling[i] = this.plotworld.MAIN_BLOCK[i].id;
        }
        
        if (this.filling.length > 1 || this.plotfloors.length > 1) {
            this.doState = true;
        }
        
        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;

        if ((this.pathsize % 2) == 0) {
            this.pathWidthLower = (short) (Math.floor(this.pathsize / 2) - 1);
        } else {
            this.pathWidthLower = (short) (Math.floor(this.pathsize / 2));
        }
        
        this.pathWidthUpper = (short) (this.pathWidthLower + this.plotsize + 1);
        
        this.biome = this.plotworld.PLOT_BIOME;
        
        try {
            maxY = Bukkit.getWorld(world).getMaxHeight();
        }
        catch (NullPointerException e) {
            maxY = 256;
        }
    }

    /**
     * Return the plot manager for this type of generator, or create one For square plots you may as well use the
     * default plot manager which comes with PlotSquared
     */
    @Override
    public PlotManager getPlotManager() {
        if (HybridGen.manager == null) {
            HybridGen.manager = new HybridPlotManager();
        }
        return HybridGen.manager;
    }

    /**
     * Allow spawning everywhere
     */
    @Override
    public boolean canSpawn(final World world, final int x, final int z) {
        return true;
    }

    /**
     * Get a new plotworld class For square plots you can use the DefaultPlotWorld class which comes with PlotSquared
     */
    @Override
    public PlotWorld getNewPlotWorld(final String world) {
        if (this.plotworld == null) {
            this.plotworld = new HybridPlotWorld(world);
        }
        return this.plotworld;
    }

    public final long nextLong() {
        final long a = this.state;
        this.state = xorShift64(a);
        return a;
    }

    public final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public final int random(final int n) {
        final long r = ((nextLong() >>> 32) * n) >> 32;
        return (int) r;
    }
    private void setBlock(short[][] result, int x, int y, int z, short[] blkids) {
        if (blkids.length == 1) {
            setBlock(result, x, y, z, blkids[0]);
        }
        else {
            final int i = random(blkids.length);
            setBlock(result, x, y, z, blkids[i]);
        }
    }
    /**
     * Standard setblock method for world generation
     */
    private void setBlock(short[][] result, int x, int y, int z, short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }

    /**
     * Return the block populator
     */
    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        // disabling spawning for this world
        if (!this.plotworld.MOB_SPAWNING) {
            world.setSpawnFlags(false, false);
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setWaterAnimalSpawnLimit(0);
        }
        // You can have as many populators as you would like, e.g. tree
        // populator, ore populator
        return Arrays.asList((BlockPopulator) new HybridPop(this.plotworld));
    }

    /**
     * Return the default spawn location for this world
     */
    @Override
    public Location getFixedSpawnLocation(final World world, final Random random) {
        return new Location(world, 0, this.plotworld.ROAD_HEIGHT + 2, 0);
    }

    /**
     * This part is a fucking mess. - Refer to a proper tutorial if you would like to learn how to make a world
     * generator
     */
    @Override
    public short[][] generateExtBlockSections(final World world, final Random random, int cx, int cz, final BiomeGrid biomes) {
        if (doState) {
            final int prime = 13;
            int h = 1;
            h = (prime * h) + cx;
            h = (prime * h) + cz;
            this.state = h;
        }
        
        this.result = new short[maxY / 16][];
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                setBlock(this.result, x, 0, z, (short) 7);
            }
        }
        HybridPlotManager hpm = ((HybridPlotManager) HybridGen.manager); 
        RegionWrapper plot = hpm.CURRENT_PLOT_CLEAR;
        if (plot != null) {
            
            int X = cx << 4;
            int Z = cz << 4;
            
            int sx = ((X) % this.size);
            int sz = ((Z) % this.size);
            
            if (sx < 0) {
                sx += this.size;
            }
            
            if (sz < 0) {
                sz += this.size;
            }
            
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    biomes.setBiome(x, z, this.biome);
                    if (isIn(plot, X + x, Z + z)) {
                        for (short y = 1; y < this.plotheight; y++) {
                            setBlock(this.result, x, y, z, this.filling);
                        }
                        setBlock(this.result, x, this.plotheight, z, this.plotfloors);
                    }
                    else {
                        ChunkLoc loc = new ChunkLoc(X + x, Z + z);
                        HashMap<Short, Short> blocks = hpm.GENERATE_BLOCKS.get(loc);
                        if (blocks != null) {
                            for (short y : blocks.keySet()) {
                                setBlock(this.result, x, y, z, blocks.get(y).shortValue());
                            }
                        }
                    }
                }
            }
            
            return this.result;
        }
        
        int sx = ((cx << 4) % this.size);
        int sz = ((cz << 4) % this.size);
        
        if (sx < 0) {
            sx += this.size;
        }
        
        if (sz < 0) {
            sz += this.size;
        }

        
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                
                if (biomes != null) {
                    biomes.setBiome(x, z, this.biome);
                }
                
                int absX = ((sx + x) % this.size);
                int absZ = ((sz + z) % this.size);
                
                boolean gx = absX > pathWidthLower;
                boolean gz = absZ > pathWidthLower;
                
                boolean lx = absX < pathWidthUpper;
                boolean lz = absZ < pathWidthUpper;
                
                // inside plot
                if (gx && gz && lx && lz) {
                    for (short y = 1; y < this.plotheight; y++) {
                        setBlock(this.result, x, y, z, this.filling);
                    }
                    setBlock(this.result, x, this.plotheight, z, this.plotfloors);
                } else {
                    // wall
                    if ((absX >= pathWidthLower && absX <= pathWidthUpper && absZ >= pathWidthLower && absZ <= pathWidthUpper))
                    {
                        for (short y = 1; y <= this.wallheight; y++) {
                            setBlock(this.result, x, y, z, this.wallfilling);
                        }
                        if (!this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(this.result, x, this.wallheight + 1, z, this.wall);
                        }
                    }
                    // road
                    else {
                        for (short y = 1; y <= this.roadheight; y++) {
                            setBlock(this.result, x, y, z, this.roadblock);
                        }
                    }
                    if (this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                        ChunkLoc loc = new ChunkLoc(absX, absZ);
                        HashMap<Short, Short> blocks = this.plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            for (short y : blocks.keySet()) {
                                setBlock(this.result, x, this.roadheight + y, z, blocks.get(y));        
                            }
                        }
                    }
                }
            }
        }
        return this.result;
    }
    
    public boolean isIn(RegionWrapper plot, int x, int z) {
        return (x >= plot.minX && x <= plot.maxX && z >= plot.minZ && z <= plot.maxZ);
    }
}
