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

import com.plotsquared.bukkit.queue.LimitedRegionWrapperQueue;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.UncheckedWorldLocation;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;

final class BlockStatePopulator extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;

    /**
     * @since TODO
     */
    public BlockStatePopulator(
            final @NonNull IndependentPlotGenerator plotGenerator
    ) {
        this.plotGenerator = plotGenerator;
    }

    /**
     * @deprecated Use {@link BlockStatePopulator#BlockStatePopulator(IndependentPlotGenerator)} as plotAreManager is unused
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public BlockStatePopulator(
            final @NonNull IndependentPlotGenerator plotGenerator,
            final @NonNull PlotAreaManager plotAreaManager
    ) {
        this.plotGenerator = plotGenerator;
    }

    @Override
    public void populate(
            @NonNull final WorldInfo worldInfo,
            @NonNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NonNull final LimitedRegion limitedRegion
    ) {
        PlotArea area = UncheckedWorldLocation.at(worldInfo.getName(), chunkX << 4, 0, chunkZ << 4).getPlotArea();
        if (area == null || (area instanceof HybridPlotWorld hpw && !hpw.populationNeeded()) || area instanceof SinglePlotArea) {
            return;
        }
        LimitedRegionWrapperQueue wrapped = new LimitedRegionWrapperQueue(limitedRegion);
        // It is possible for the region to be larger than the chunk, but there is no reason for P2 to need to populate
        // outside of the actual chunk area.
        Location min = UncheckedWorldLocation.at(worldInfo.getName(), chunkX << 4, worldInfo.getMinHeight(), chunkZ << 4);
        Location max = UncheckedWorldLocation.at(
                worldInfo.getName(),
                (chunkX << 4) + 15,
                worldInfo.getMaxHeight(),
                (chunkZ << 4) + 15
        );
        ScopedQueueCoordinator offsetChunkQueue = new ScopedQueueCoordinator(wrapped, min, max);
        this.plotGenerator.populateChunk(offsetChunkQueue, area);
    }

}
