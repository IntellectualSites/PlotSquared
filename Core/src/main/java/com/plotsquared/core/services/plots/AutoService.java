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
package com.plotsquared.core.services.plots;

import cloud.commandframework.services.types.Service;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public interface AutoService extends Service<AutoService.AutoQuery, List<Plot>> {

    Cache<PlotId, Plot> plotCandidateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.SECONDS).build();
    Object plotLock = new Object();

    record AutoQuery(PlotPlayer<?> player, PlotId startId, int sizeX, int sizeZ, PlotArea plotArea) {

        /**
         * Crate a new auto query
         *
         * @param player   Player to claim for
         * @param startId  Plot ID to start searching from
         * @param sizeX    Number of plots along the X axis
         * @param sizeZ    Number of plots along the Z axis
         * @param plotArea Plot area to search in
         */
        public AutoQuery(
                final @NonNull PlotPlayer<?> player, final @Nullable PlotId startId,
                final int sizeX, final int sizeZ, final @NonNull PlotArea plotArea
        ) {
            this.player = player;
            this.startId = startId;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
            this.plotArea = plotArea;
        }

        /**
         * Get the player that the plots are meant for
         *
         * @return Player
         */
        @Override
        public @NonNull PlotPlayer<?> player() {
            return this.player;
        }

        /**
         * Get the plot ID to start searching from
         *
         * @return Start ID
         */
        @Override
        public @Nullable PlotId startId() {
            return this.startId;
        }

        /**
         * Get the number of plots along the X axis
         *
         * @return Number of plots along the X axis
         */
        @Override
        public int sizeX() {
            return this.sizeX;
        }

        /**
         * Get the number of plots along the Z axis
         *
         * @return Number of plots along the Z axis
         */
        @Override
        public int sizeZ() {
            return this.sizeZ;
        }

        /**
         * Get the plot area to search in
         *
         * @return Plot area
         */
        @Override
        public @NonNull PlotArea plotArea() {
            return this.plotArea;
        }

    }


    final class DefaultAutoService implements AutoService {

        @Override
        public List<Plot> handle(final @NonNull AutoQuery autoQuery) {
            return Collections.emptyList();
        }

    }


    final class SinglePlotService implements AutoService, Predicate<AutoQuery> {

        @Nullable
        @Override
        public List<Plot> handle(@NonNull AutoQuery autoQuery) {
            Plot plot;
            PlotId nextId = autoQuery.startId();
            do {
                synchronized (plotLock) {
                    plot = autoQuery.plotArea().getNextFreePlot(autoQuery.player(), nextId);
                    if (plot != null && plotCandidateCache.getIfPresent(plot.getId()) == null) {
                        plotCandidateCache.put(plot.getId(), plot);
                        return Collections.singletonList(plot);
                    }
                    // if the plot is already in the cache, we want to make sure we skip it the next time
                    if (plot != null) {
                        nextId = plot.getId();
                    }
                }
            } while (plot != null);
            return null;
        }

        @Override
        public boolean test(final @NonNull AutoQuery autoQuery) {
            return autoQuery.sizeX == 1 && autoQuery.sizeZ == 1;
        }

    }


    final class MultiPlotService implements AutoService, Predicate<AutoQuery> {

        @Override
        public List<Plot> handle(final @NonNull AutoQuery autoQuery) {
            /* TODO: Add timeout? */
            outer:
            while (true) {
                synchronized (plotLock) {
                    final PlotId start =
                            autoQuery.plotArea().getMeta("lastPlot", PlotId.of(0, 0)).getNextId();
                    final PlotId end = PlotId.of(
                            start.getX() + autoQuery.sizeX() - 1,
                            start.getY() + autoQuery.sizeZ() - 1
                    );
                    final List<Plot> plots =
                            autoQuery.plotArea().canClaim(autoQuery.player(), start, end);
                    autoQuery.plotArea().setMeta("lastPlot", start); // set entry point for next try
                    if (plots != null && !plots.isEmpty()) {
                        for (final Plot plot : plots) {
                            if (plotCandidateCache.getIfPresent(plot.getId()) != null) {
                                continue outer;
                            }
                            plotCandidateCache.put(plot.getId(), plot);
                        }
                        return plots;
                    }
                }
            }
        }

        @Override
        public boolean test(final @NonNull AutoQuery autoQuery) {
            return autoQuery.plotArea().getType() != PlotAreaType.PARTIAL;
        }

    }

}
