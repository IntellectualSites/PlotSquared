package com.plotsquared.events;

import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotId;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Event called when plots are automatically merged with /plot auto
 * {@inheritDoc}
 */
public final class PlotAutoMergeEvent extends PlotEvent implements CancellablePlotEvent {

    private final List<PlotId> plots;
    @Getter private final String world;
    private Result eventResult;

    /**
     * PlotAutoMergeEvent: Called when plots are automatically merged with /plot auto
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotAutoMergeEvent(@NotNull final String world, @NotNull final Plot plot,
        @NotNull final List<PlotId> plots) {
        super(plot);
        this.world = world;
        this.plots = plots;
    }
    /**
     * Get the plots being added.
     *
     * @return Unmodifiable list containing the merging plots
     */
    public List<PlotId> getPlots() {
        return Collections.unmodifiableList(this.plots);
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
