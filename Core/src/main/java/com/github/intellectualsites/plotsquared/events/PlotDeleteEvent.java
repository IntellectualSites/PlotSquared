package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;

/**
 * Called when a plot is deleted
 */
public class PlotDeleteEvent extends PlotEvent implements CancellablePlotEvent {

    private Result eventResult;

    public PlotDeleteEvent(Plot plot) {
        super(plot);
    }

    /**
     * Get the PlotId.
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name.
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
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
