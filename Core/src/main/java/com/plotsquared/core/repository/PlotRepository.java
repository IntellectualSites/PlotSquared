package com.plotsquared.core.repository;

import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.repository.dbo.PlotDBO;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.stream.Stream;

/**
 * {@link Repository} storing {@link PlotDBO plots} identified by their respective {@link PlotId}.
 */
public interface PlotRepository extends Repository<PlotDBO, Integer> {

    /**
     * Returns all plots in the given {@code area}.
     *
     * @param area the plot area
     * @return a stream with all plots in the given area.
     */
    @NonNull Stream<PlotDBO> getPlotsInArea(@NonNull String area);
}
