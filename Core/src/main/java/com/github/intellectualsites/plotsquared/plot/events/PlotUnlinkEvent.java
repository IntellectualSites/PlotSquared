package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Event called when several merged plots are unlinked
 * {@inheritDoc}
 */
public final class PlotUnlinkEvent extends PlotEvent implements CancellablePlotEvent {

    private final List<PlotId> plots;
    @Getter private final String world;
    @Getter private final PlotArea area;
    private Result eventResult = Result.ACCEPT;

    /**
     * Called when a mega-plot is unlinked.
     *
     * @param world World in which the event occurred
     * @param plots Plots that are involved in the event
     */
    public PlotUnlinkEvent(@NotNull final String world, @NotNull final PlotArea area,
        @NotNull final List<PlotId> plots, Plot plot) {
        super(plot);
        this.plots = plots;
        this.world = world;
        this.area = area;
    }

    /**
     * Get the plots involved.
     *
     * @return Unmodifiable list containing {@link PlotId PlotIds} of the plots involved
     */
    public List<PlotId> getPlots() {
        return Collections.unmodifiableList(this.plots);
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
