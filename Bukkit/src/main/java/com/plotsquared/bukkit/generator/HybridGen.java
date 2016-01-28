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

import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * The default generator is very messy, as we have decided to try externalize all calculations from within the loop. -
 * You will see a lot of slower implementations have a single for loop. - This is perfectly fine to do, it will just
 * mean world generation may take somewhat longer
 *


 */
public class HybridGen extends BukkitPlotGenerator {
    
    public HybridGen(final String world) {
        super(world);
    }
    
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
    int plotsize;
    int pathsize;
    short wall;
    short wallfilling;
    short roadblock;
    int size;
    Biome biome;
    int roadheight;
    int wallheight;
    int plotheight;
    short[] plotfloors;
    short[] filling;
    short pathWidthLower;
    short pathWidthUpper;
    boolean doState = false;
    int maxY = 0;
    short[][] cached;
    
    /**
     * Initialize variables, and create plotworld object used in calculations
     */
    @Override
    public void init(final PlotWorld plotworld) {
        if (plotworld != null) {
            this.plotworld = (HybridPlotWorld) plotworld;
        }
        plotsize = this.plotworld.PLOT_WIDTH;
        pathsize = this.plotworld.ROAD_WIDTH;
        roadblock = this.plotworld.ROAD_BLOCK.id;
        wallfilling = this.plotworld.WALL_FILLING.id;
        size = pathsize + plotsize;
        wall = this.plotworld.WALL_BLOCK.id;
        plotfloors = new short[this.plotworld.TOP_BLOCK.length];
        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++) {
            plotfloors[i] = this.plotworld.TOP_BLOCK[i].id;
        }
        filling = new short[this.plotworld.MAIN_BLOCK.length];
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++) {
            filling[i] = this.plotworld.MAIN_BLOCK[i].id;
        }
        if ((filling.length > 1) || (plotfloors.length > 1)) {
            doState = true;
        }
        wallheight = this.plotworld.WALL_HEIGHT;
        roadheight = this.plotworld.ROAD_HEIGHT;
        plotheight = this.plotworld.PLOT_HEIGHT;
        if (pathsize == 0) {
            pathWidthLower = (short) -1;
            pathWidthUpper = (short) (plotsize + 1);
        } else {
            if ((pathsize % 2) == 0) {
                pathWidthLower = (short) (Math.floor(pathsize / 2) - 1);
            } else {
                pathWidthLower = (short) (Math.floor(pathsize / 2));
            }
            pathWidthUpper = (short) (pathWidthLower + plotsize + 1);
        }
        biome = Biome.valueOf(this.plotworld.PLOT_BIOME);
        try {
            maxY = Bukkit.getWorld(plotworld.worldname).getMaxHeight();
        } catch (final NullPointerException ignored) {}
        if (maxY == 0) {
            maxY = 256;
        }
        
        // create cached chunk (for optimized chunk generation)
        if (!this.plotworld.PLOT_SCHEMATIC) {
            cached = new short[(plotheight + 16) / 16][];
            for (int i = 0; i < cached.length; i++) {
                cached[i] = new short[4096];
            }
            random.state = 7919;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    cached[CACHE_I[plotheight][x][z]][CACHE_J[plotheight][x][z]] = plotfloors[random.random(plotfloors.length)];
                    if (this.plotworld.PLOT_BEDROCK) {
                        cached[CACHE_I[0][x][z]][CACHE_J[0][x][z]] = 7;
                    }
                    for (int y = 1; y < plotheight; y++) {
                        cached[CACHE_I[y][x][z]][CACHE_J[y][x][z]] = filling[random.random(filling.length)];
                    }
                }
            }
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
     * Get a new plotworld class For square plots you can use the DefaultPlotWorld class which comes with PlotSquared
     */
    @Override
    public PlotWorld getNewPlotWorld(final String world) {
        if (plotworld == null) {
            plotworld = new HybridPlotWorld(world);
        }
        return plotworld;
    }
    
    /**
     * Return the block populator
     */
    @Override
    public List<BukkitPlotPopulator> getPopulators(final String world) {
        // You can have as many populators as you would like, e.g. tree
        // populator, ore populator
        return Collections.singletonList((BukkitPlotPopulator) new HybridPop(plotworld));
    }
    
    /**
     * This part is a fucking mess. - Refer to a proper tutorial if you would like to learn how to make a world
     * generator
     */
    @Override
    public void generateChunk(final World world, final RegionWrapper region, final PseudoRandom random, final int cx, final int cz, final BiomeGrid biomes) {
        int sx = (short) ((X - plotworld.ROAD_OFFSET_X) % size);
        int sz = (short) ((Z - plotworld.ROAD_OFFSET_Z) % size);
        if (sx < 0) {
            sx += size;
        }
        if (sz < 0) {
            sz += size;
        }
        
        if (biomes != null) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    biomes.setBiome(x, z, biome);
                }
            }
        }
        
        if (cached != null) {
            if ((sx > pathWidthLower) && (sz > pathWidthLower) && ((sx + 15) < pathWidthUpper) && ((sz + 15) < pathWidthUpper)) {
                setResult(cached);
                return;
            }
        }

        if (region != null) {
            for (short x = 0; x < 16; x++) {
                final int absX = ((sx + x) % size);
                for (short z = 0; z < 16; z++) {
                    if (contains(region, x, z)) {
                        setBlock(x, 0, z, (short) 7);
                        setBlock(x, plotheight, z, plotfloors);
                        for (short y = 1; y < plotheight; y++) {
                            setBlock(x, y, z, filling);
                        }
                        final int absZ = ((sz + z) % size);
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            for (final Entry<Short, Short> entry : blocks.entrySet()) {
                                setBlock(x, plotheight + entry.getKey(), z, entry.getValue());
                            }
                        }
                    }
                }
            }
            return;
        }
        
        if (plotworld.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    setBlock(x, 0, z, (short) 7);
                }
            }
        }

        for (short x = 0; x < 16; x++) {
            final int absX = ((sx + x) % size);
            final boolean gx = absX > pathWidthLower;
            final boolean lx = absX < pathWidthUpper;
            for (short z = 0; z < 16; z++) {
                final int absZ = ((sz + z) % size);
                final boolean gz = absZ > pathWidthLower;
                final boolean lz = absZ < pathWidthUpper;
                // inside plot
                if (gx && gz && lx && lz) {
                    setBlock(x, plotheight, z, plotfloors);
                    for (short y = 1; y < plotheight; y++) {
                        setBlock(x, y, z, filling);
                    }
                    if (plotworld.PLOT_SCHEMATIC) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            for (final Entry<Short, Short> entry : blocks.entrySet()) {
                                setBlock(x, plotheight + entry.getKey(), z, entry.getValue());
                            }
                        }
                    }
                } else if (pathsize != 0) {
                    // wall
                    if (((absX >= pathWidthLower) && (absX <= pathWidthUpper) && (absZ >= pathWidthLower) && (absZ <= pathWidthUpper))) {
                        for (short y = 1; y <= wallheight; y++) {
                            setBlock(x, y, z, wallfilling);
                        }
                        if (!plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(x, wallheight + 1, z, wall);
                        }
                    }
                    // road
                    else {
                        for (short y = 1; y <= roadheight; y++) {
                            setBlock(x, y, z, roadblock);
                        }
                    }
                    if (plotworld.ROAD_SCHEMATIC_ENABLED) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            for (final Entry<Short, Short> entry : blocks.entrySet()) {
                                setBlock(x, roadheight + entry.getKey(), z, entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }
}
