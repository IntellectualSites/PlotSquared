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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.bukkit.generator;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

final class DelegatePlotGenerator extends IndependentPlotGenerator {

    private final ChunkGenerator chunkGenerator;
    private final String world;

    public DelegatePlotGenerator(ChunkGenerator chunkGenerator, String world) {
        this.chunkGenerator = chunkGenerator;
        this.world = world;
    }

    @Override public void initialize(PlotArea area) {
    }

    @Override public String getName() {
        return this.chunkGenerator.getClass().getName();
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return PlotSquared.platform().getDefaultGenerator().getNewPlotArea(world, id, min, max);
    }

    @Override public void generateChunk(final ScopedQueueCoordinator result, PlotArea settings) {
        World world = BukkitUtil.getWorld(this.world);
        Location min = result.getMin();
        int chunkX = min.getX() >> 4;
        int chunkZ = min.getZ() >> 4;
        Random random = new Random(MathMan.pair((short) chunkX, (short) chunkZ));
        try {
            ChunkGenerator.BiomeGrid grid = new ChunkGenerator.BiomeGrid() {
                @Override public void setBiome(int x, int z, @Nonnull Biome biome) {
                    result.setBiome(x, z, BukkitAdapter.adapt(biome));
                }

                //do not annotate with Override until we discontinue support for 1.4.4
                public void setBiome(int x, int y, int z, @Nonnull Biome biome) {
                    result.setBiome(x, z, BukkitAdapter.adapt(biome));

                }

                @Override @Nonnull public Biome getBiome(int x, int z) {
                    return Biome.FOREST;
                }

                @Override public @Nonnull Biome getBiome(int x, int y, int z) {
                    return Biome.FOREST;
                }
            };
            chunkGenerator.generateChunkData(world, random, chunkX, chunkZ, grid);
            return;
        } catch (Throwable ignored) {
        }
        for (BlockPopulator populator : chunkGenerator.getDefaultPopulators(world)) {
            populator.populate(world, random, world.getChunkAt(chunkX, chunkZ));
        }
    }

}
