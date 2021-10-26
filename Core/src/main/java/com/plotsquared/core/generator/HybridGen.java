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
 *                  Copyright (C) 2021 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public class HybridGen extends IndependentPlotGenerator {

    private final HybridPlotWorldFactory hybridPlotWorldFactory;

    @Inject
    public HybridGen(final @NonNull HybridPlotWorldFactory hybridPlotWorldFactory) {
        this.hybridPlotWorldFactory = hybridPlotWorldFactory;
    }

    @Override
    public String getName() {
        return PlotSquared.platform().pluginName();
    }

    private void placeSchem(
            HybridPlotWorld world, ScopedQueueCoordinator result, short relativeX,
            short relativeZ, int x, int z, boolean isRoad
    ) {
        int minY; // Math.min(world.PLOT_HEIGHT, world.ROAD_HEIGHT);
        if ((isRoad && Settings.Schematics.PASTE_ROAD_ON_TOP) || (!isRoad
                && Settings.Schematics.PASTE_ON_TOP)) {
            minY = world.SCHEM_Y;
        } else {
            minY = world.getMinBuildHeight();
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

    @Override
    public void generateChunk(@NonNull ScopedQueueCoordinator result, @NonNull PlotArea settings) {
        Preconditions.checkNotNull(result, "result cannot be null");
        Preconditions.checkNotNull(settings, "settings cannot be null");

        HybridPlotWorld hybridPlotWorld = (HybridPlotWorld) settings;
        // Biome
        result.fillBiome(hybridPlotWorld.getPlotBiome());
        // Bedrock
        if (hybridPlotWorld.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    result.setBlock(x, 0, z, BlockTypes.BEDROCK.getDefaultState());
                }
            }
        }
        // Coords
        Location min = result.getMin();
        int bx = (min.getX()) - hybridPlotWorld.ROAD_OFFSET_X;
        int bz = (min.getZ()) - hybridPlotWorld.ROAD_OFFSET_Z;
        // The relative X-coordinate (within the plot) of the minimum X coordinate
        // contained in the scoped queue
        short relativeOffsetX;
        if (bx < 0) {
            relativeOffsetX = (short) (hybridPlotWorld.SIZE + (bx % hybridPlotWorld.SIZE));
        } else {
            relativeOffsetX = (short) (bx % hybridPlotWorld.SIZE);
        }
        // The relative Z-coordinate (within the plot) of the minimum Z coordinate
        // contained in the scoped queue
        short relativeOffsetZ;
        if (bz < 0) {
            relativeOffsetZ = (short) (hybridPlotWorld.SIZE + (bz % hybridPlotWorld.SIZE));
        } else {
            relativeOffsetZ = (short) (bz % hybridPlotWorld.SIZE);
        }
        // The X-coordinate of a given X coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeX = new short[16];
        boolean[] insideRoadX = new boolean[16];
        boolean[] insideWallX = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (relativeOffsetX + i);
            while (v >= hybridPlotWorld.SIZE) {
                v -= hybridPlotWorld.SIZE;
            }
            relativeX[i] = v;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                insideRoadX[i] =
                        v < hybridPlotWorld.PATH_WIDTH_LOWER || v > hybridPlotWorld.PATH_WIDTH_UPPER;
                insideWallX[i] =
                        v == hybridPlotWorld.PATH_WIDTH_LOWER || v == hybridPlotWorld.PATH_WIDTH_UPPER;
            }
        }
        // The Z-coordinate of a given Z coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeZ = new short[16];
        // Whether or not the given Z coordinate belongs to the road
        boolean[] insideRoadZ = new boolean[16];
        // Whether or not the given Z coordinate belongs to the wall
        boolean[] insideWallZ = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (relativeOffsetZ + i);
            while (v >= hybridPlotWorld.SIZE) {
                v -= hybridPlotWorld.SIZE;
            }
            relativeZ[i] = v;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                insideRoadZ[i] =
                        v < hybridPlotWorld.PATH_WIDTH_LOWER || v > hybridPlotWorld.PATH_WIDTH_UPPER;
                insideWallZ[i] =
                        v == hybridPlotWorld.PATH_WIDTH_LOWER || v == hybridPlotWorld.PATH_WIDTH_UPPER;
            }
        }
        // generation
        int startY = hybridPlotWorld.PLOT_BEDROCK ? 1 : 0;
        for (short x = 0; x < 16; x++) {
            if (insideRoadX[x]) {
                for (short z = 0; z < 16; z++) {
                    // Road
                    for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                        result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                    }
                    if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                        placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, true);
                    }
                }
            } else if (insideWallX[x]) {
                for (short z = 0; z < 16; z++) {
                    if (insideRoadZ[z]) {
                        // road
                        for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                        }
                        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z,
                                    true
                            );
                        }
                    } else {
                        // wall
                        for (int y = startY; y <= hybridPlotWorld.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.WALL_FILLING.toPattern());
                        }
                        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            if (hybridPlotWorld.PLACE_TOP_BLOCK) {
                                result.setBlock(x, hybridPlotWorld.WALL_HEIGHT + 1, z,
                                        hybridPlotWorld.WALL_BLOCK.toPattern()
                                );
                            }
                        } else {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z,
                                    true
                            );
                        }
                    }
                }
            } else {
                for (short z = 0; z < 16; z++) {
                    if (insideRoadZ[z]) {
                        // road
                        for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                        }
                        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z,
                                    true
                            );
                        }
                    } else if (insideWallZ[z]) {
                        // wall
                        for (int y = startY; y <= hybridPlotWorld.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.WALL_FILLING.toPattern());
                        }
                        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            if (hybridPlotWorld.PLACE_TOP_BLOCK) {
                                result.setBlock(x, hybridPlotWorld.WALL_HEIGHT + 1, z,
                                        hybridPlotWorld.WALL_BLOCK.toPattern()
                                );
                            }
                        } else {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z,
                                    true
                            );
                        }
                    } else {
                        // plot
                        for (int y = startY; y < hybridPlotWorld.PLOT_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.MAIN_BLOCK.toPattern());
                        }
                        result.setBlock(x, hybridPlotWorld.PLOT_HEIGHT, z,
                                hybridPlotWorld.TOP_BLOCK.toPattern()
                        );
                        if (hybridPlotWorld.PLOT_SCHEMATIC) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z,
                                    false
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return this.hybridPlotWorldFactory.create(world, id, this, min, max);
    }

    @Override
    public void initialize(PlotArea area) {
        // All initialization is done in the PlotArea class
    }

}
