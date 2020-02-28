package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotEvent implements CancellablePlotEvent {

    @Getter private final int dir;
    @Getter private final int max;
    @Getter private final String world;
    private Result eventResult;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param dir   The direction of the merge
     * @param max   Max merge size
     */
    public PlotMergeEvent(@NotNull final String world, @NotNull final Plot plot,
        @NotNull final int dir, @NotNull final int max) {
        super(plot);
        this.world = world;
        this.dir = dir;
        this.max = max;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public int getEventResultRaw() {
        return eventResult.getValue();
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
