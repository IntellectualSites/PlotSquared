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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.UncheckedWorldLocation;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

final class BlockStatePopulator116 extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;

    /**
     * @since TODO
     */
    public BlockStatePopulator116(
            final @NonNull IndependentPlotGenerator plotGenerator
    ) {
        this.plotGenerator = plotGenerator;
    }

    @Override
    public void populate(@NotNull final World world, @NotNull final Random random, @NotNull final Chunk source) {
        int chunkMinX = source.getX() << 4;
        int chunkMinZ = source.getZ() << 4;
        PlotArea area = Location.at(world.getName(), chunkMinX, 0, chunkMinZ).getPlotArea();
        if (area == null || (area instanceof HybridPlotWorld hpw && !hpw.populationNeeded()) || area instanceof SinglePlotArea) {
            return;
        }

        QueueCoordinator queue = PlotSquared.platform().globalBlockQueue().getNewQueue(new BukkitWorld(world));
        queue.setForceSync(true);
        queue.setSideEffectSet(SideEffectSet.none());
        queue.setBiomesEnabled(false);
        queue.setChunkObject(source);
        Location min = UncheckedWorldLocation.at(world.getName(), chunkMinX, world.getMinHeight(), chunkMinZ);
        Location max = UncheckedWorldLocation.at(world.getName(), chunkMinX + 15, world.getMaxHeight(), chunkMinZ + 15);
        ScopedQueueCoordinator offsetChunkQueue = new ScopedQueueCoordinator(queue, min, max);
        this.plotGenerator.populateChunk(offsetChunkQueue, area);
        queue.enqueue();
    }

}
