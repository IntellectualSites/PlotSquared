package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

/**
 * Called when a flag is removed from a plot
 */
public class PlotFlagRemoveEvent extends PlotFlagEvent implements CancellablePlotEvent {

    private Result eventResult;

    /**
     * PlotFlagRemoveEvent: Called when a flag is removed from a plot
     *
     * @param flag Flag that was removed
     * @param plot Plot from which the flag was removed
     */
    public PlotFlagRemoveEvent(PlotFlag<?, ?> flag, Plot plot) {
        super(plot, flag);
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
