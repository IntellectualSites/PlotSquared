package com.plotsquared.core.services.plots;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellectualsites.services.types.Service;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public interface AutoService extends Service<AutoService.AutoQuery, List<Plot>> {

    Cache<PlotId, Plot> plotCandidateCache = CacheBuilder.newBuilder()
        .expireAfterWrite(20, TimeUnit.SECONDS).build();
    Object plotLock = new Object();

    final class AutoQuery {

        private final PlotPlayer<?> player;
        private final PlotId startId;
        private final int sizeX;
        private final int sizeZ;
        private final PlotArea plotArea;

        /**
         * Crate a new auto query
         *
         * @param player   Player to claim for
         * @param startId  Plot ID to start searching from
         * @param sizeX    Number of plots along the X axis
         * @param sizeZ    Number of plots along the Z axis
         * @param plotArea Plot area to search in
         */
        public AutoQuery(@Nonnull final PlotPlayer<?> player, @Nullable final PlotId startId,
            final int sizeX, final int sizeZ, @Nonnull final PlotArea plotArea) {
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
        @Nonnull public PlotPlayer<?> getPlayer() {
            return this.player;
        }

        /**
         * Get the plot ID to start searching from
         *
         * @return Start ID
         */
        @Nullable public PlotId getStartId() {
            return this.startId;
        }

        /**
         * Get the number of plots along the X axis
         *
         * @return Number of plots along the X axis
         */
        public int getSizeX() {
            return this.sizeX;
        }

        /**
         * Get the number of plots along the Z axis
         *
         * @return Number of plots along the Z axis
         */
        public int getSizeZ() {
            return this.sizeZ;
        }

        /**
         * Get the plot area to search in
         *
         * @return Plot area
         */
        @Nonnull public PlotArea getPlotArea() {
            return this.plotArea;
        }

    }


    final class DefaultAutoService implements AutoService {

        @Override public List<Plot> handle(@Nonnull final AutoQuery autoQuery) {
            return Collections.emptyList();
        }

    }


    final class SinglePlotService implements AutoService, Predicate<AutoQuery> {

        @Nullable @Override public List<Plot> handle(@Nonnull AutoQuery autoQuery) {
            Plot plot;
            do {
                synchronized (plotLock) {
                    plot = autoQuery.getPlotArea().getNextFreePlot(autoQuery.getPlayer(), autoQuery.getStartId());
                    if (plot != null && plotCandidateCache.getIfPresent(plot.getId()) == null) {
                        plotCandidateCache.put(plot.getId(), plot);
                        return Collections.singletonList(plot);
                    }
                }
            } while (plot != null);
            return null;
        }

        @Override public boolean test(@Nonnull final AutoQuery autoQuery) {
            return autoQuery.sizeX == 1 && autoQuery.sizeZ == 1;
        }

    }


    final class MultiPlotService implements AutoService, Predicate<AutoQuery> {

        @Override public List<Plot> handle(@Nonnull final AutoQuery autoQuery) {
            /* TODO: Add timeout? */
            outer: while (true) {
                synchronized (plotLock) {
                    final PlotId start =
                        autoQuery.getPlotArea().getMeta("lastPlot", PlotId.of(0, 0)).getNextId();
                    final PlotId end = PlotId.of(start.getX() + autoQuery.getSizeX() - 1,
                        start.getY() + autoQuery.getSizeZ() - 1);
                    final List<Plot> plots =
                        autoQuery.getPlotArea().canClaim(autoQuery.getPlayer(), start, end);
                    if (plots != null && !plots.isEmpty()) {
                        autoQuery.getPlotArea().setMeta("lastPlot", start);
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

        @Override public boolean test(@Nonnull final AutoQuery autoQuery) {
            return autoQuery.getPlotArea().getType() != PlotAreaType.PARTIAL;
        }

    }

}
