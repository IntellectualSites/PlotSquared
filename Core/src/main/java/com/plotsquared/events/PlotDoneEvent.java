package com.plotsquared.events;

import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotId;

/**
 * Called when a plot is cleared
 */
public class PlotDoneEvent extends PlotEvent implements CancellablePlotEvent {

    private Result eventResult;

    /**
     * PlotDoneEvent: Called when a plot is being set as done
     *
     * @param plot The plot being set as done
     */
    public PlotDoneEvent(Plot plot) {
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

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
