/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;

final class LegacyBlockStatePopulator extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;

    /**
     * @since 6.9.0
     */
    public LegacyBlockStatePopulator(
            final @NonNull IndependentPlotGenerator plotGenerator
    ) {
        this.plotGenerator = plotGenerator;
    }

    @Override
    public void populate(@NonNull final World world, @NonNull final Random random, @NonNull final Chunk source) {
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
        ZeroedDelegateScopedQueueCoordinator offsetChunkQueue = new ZeroedDelegateScopedQueueCoordinator(queue, min, max);
        this.plotGenerator.populateChunk(offsetChunkQueue, area);
        queue.enqueue();
    }

}
