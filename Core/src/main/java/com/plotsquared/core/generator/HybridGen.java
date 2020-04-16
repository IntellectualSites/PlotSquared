/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

public class HybridGen extends IndependentPlotGenerator {

    @Override public String getName() {
        return PlotSquared.imp().getPluginName();
    }

    private void placeSchem(HybridPlotWorld world, ScopedLocalBlockQueue result, short relativeX,
        short relativeZ, int x, int z, boolean isRoad) {
        int minY; // Math.min(world.PLOT_HEIGHT, world.ROAD_HEIGHT);
        if (isRoad || Settings.Schematics.PASTE_ON_TOP) {
            minY = world.SCHEM_Y;
        } else {
            minY = 1;
        }
        BaseBlock[] blocks = world.G_SCH.get(MathMan.pair(relativeX, relativeZ));
        if (blocks != null) {
            for (int y = 0; y < blocks.length; y++) {
                if (blocks[y] != null) {
                    result.setBlock(x, minY + y, z, blocks[y]);
                }
            }
        }
        BiomeType biome = world.G_SCH_B.get(MathMan.pair(relativeX, relativeZ));
        if (biome != null) {
            result.setBiome(x, z, biome);
        }
    }

    @Override public void generateChunk(@NotNull ScopedLocalBlockQueue result, @NotNull PlotArea settings) {
        Preconditions.checkNotNull(result, "result cannot be null");
        Preconditions.checkNotNull(settings, "settings cannot be null");

        HybridPlotWorld hpw = (HybridPlotWorld) settings;
        // Biome
        result.fillBiome(hpw.getPlotBiome());
        // Bedrock
        if (hpw.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    result.setBlock(x, 0, z, BlockTypes.BEDROCK.getDefaultState());
                }
            }
        }
        // Coords
        Location min = result.getMin();
        int bx = (min.getX()) - hpw.ROAD_OFFSET_X;
        int bz = (min.getZ()) - hpw.ROAD_OFFSET_Z;
        short rbx;
        if (bx < 0) {
            rbx = (short) (hpw.SIZE + (bx % hpw.SIZE));
        } else {
            rbx = (short) (bx % hpw.SIZE);
        }
        short rbz;
        if (bz < 0) {
            rbz = (short) (hpw.SIZE + (bz % hpw.SIZE));
        } else {
            rbz = (short) (bz % hpw.SIZE);
        }
        short[] rx = new short[16];
        boolean[] gx = new boolean[16];
        boolean[] wx = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (rbx + i);
            if (v >= hpw.SIZE) {
                v -= hpw.SIZE;
            }
            rx[i] = v;
            if (hpw.ROAD_WIDTH != 0) {
                gx[i] = v < hpw.PATH_WIDTH_LOWER || v > hpw.PATH_WIDTH_UPPER;
                wx[i] = v == hpw.PATH_WIDTH_LOWER || v == hpw.PATH_WIDTH_UPPER;
            }
        }
        short[] rz = new short[16];
        boolean[] gz = new boolean[16];
        boolean[] wz = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (rbz + i);
            if (v >= hpw.SIZE) {
                v -= hpw.SIZE;
            }
            rz[i] = v;
            if (hpw.ROAD_WIDTH != 0) {
                gz[i] = v < hpw.PATH_WIDTH_LOWER || v > hpw.PATH_WIDTH_UPPER;
                wz[i] = v == hpw.PATH_WIDTH_LOWER || v == hpw.PATH_WIDTH_UPPER;
            }
        }
        // generation
        for (short x = 0; x < 16; x++) {
            if (gx[x]) {
                for (short z = 0; z < 16; z++) {
                    // Road
                    for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                        result.setBlock(x, y, z, hpw.ROAD_BLOCK.toPattern());
                    }
                    if (hpw.ROAD_SCHEMATIC_ENABLED) {
                        placeSchem(hpw, result, rx[x], rz[z], x, z, true);
                    }
                }
            } else if (wx[x]) {
                for (short z = 0; z < 16; z++) {
                    if (gz[z]) {
                        // road
                        for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.ROAD_BLOCK.toPattern());
                        }
                        if (hpw.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z, true);
                        }
                    } else {
                        // wall
                        for (int y = 1; y <= hpw.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.WALL_FILLING.toPattern());
                        }
                        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
                            result.setBlock(x, hpw.WALL_HEIGHT + 1, z, hpw.WALL_BLOCK.toPattern());
                        } else {
                            placeSchem(hpw, result, rx[x], rz[z], x, z, true);
                        }
                    }
                }
            } else {
                for (short z = 0; z < 16; z++) {
                    if (gz[z]) {
                        // road
                        for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.ROAD_BLOCK.toPattern());
                        }
                        if (hpw.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z, true);
                        }
                    } else if (wz[z]) {
                        // wall
                        for (int y = 1; y <= hpw.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.WALL_FILLING.toPattern());
                        }
                        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
                            result.setBlock(x, hpw.WALL_HEIGHT + 1, z, hpw.WALL_BLOCK.toPattern());
                        } else {
                            placeSchem(hpw, result, rx[x], rz[z], x, z, true);
                        }
                    } else {
                        // plot
                        for (int y = 1; y < hpw.PLOT_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.MAIN_BLOCK.toPattern());
                        }
                        result.setBlock(x, hpw.PLOT_HEIGHT, z, hpw.TOP_BLOCK.toPattern());
                        if (hpw.PLOT_SCHEMATIC) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z, false);
                        }
                    }
                }
            }
        }
    }

/*    @Override public boolean populateChunk(ScopedLocalBlockQueue result, PlotArea settings) {
        HybridPlotWorld hpw = (HybridPlotWorld) settings;
        if (hpw.G_SCH_STATE != null) {
            Location min = result.getMin();
            int cx = min.getX() >> 4;
            int cz = min.getZ() >> 4;
            int p1x = cx << 4;
            int p1z = cz << 4;
            int bx = p1x - hpw.ROAD_OFFSET_X;
            int bz = p1z - hpw.ROAD_OFFSET_Z;
            short rbx;
            if (bx < 0) {
                rbx = (short) (hpw.SIZE + (bx % hpw.SIZE));
            } else {
                rbx = (short) (bx % hpw.SIZE);
            }
            short rbz;
            if (bz < 0) {
                rbz = (short) (hpw.SIZE + (bz % hpw.SIZE));
            } else {
                rbz = (short) (bz % hpw.SIZE);
            }
            short[] rx = new short[16];
            for (short i = 0; i < 16; i++) {
                short v = (short) (rbx + i);
                if (v >= hpw.SIZE) {
                    v -= hpw.SIZE;
                }
                rx[i] = v;
            }
            short[] rz = new short[16];
            for (short i = 0; i < 16; i++) {
                short v = (short) (rbz + i);
                if (v >= hpw.SIZE) {
                    v -= hpw.SIZE;
                }
                rz[i] = v;
            }
            LocalBlockQueue queue = null;
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    int pair = MathMan.pair(rx[x], rz[z]);
                    HashMap<Integer, CompoundTag> map = hpw.G_SCH_STATE.get(pair);
                    if (map != null) {
                        for (Entry<Integer, CompoundTag> entry : map.entrySet()) {
                            if (queue == null) {
                                queue = GlobalBlockQueue.IMP.getNewQueue(hpw.worldname, false);
                            }
                            CompoundTag tag = entry.getValue();
                            SchematicHandler.manager
                                .restoreTile(queue, tag, p1x + x, entry.getKey(), p1z + z);
                        }
                    }
                }
            }
            if (queue != null) {
                queue.flush();
            }
        }
        return false;
    }*/

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return new HybridPlotWorld(world, id, this, min, max);
    }

    @Override public void initialize(PlotArea area) {
        // All initialization is done in the PlotArea class
    }
}
