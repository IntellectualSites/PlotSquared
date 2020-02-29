package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

/**
 * Called when a Flag is added to a plot.
 */
public class PlotFlagAddEvent extends PlotFlagEvent implements CancellablePlotEvent {

    private Result eventResult;

    /**
     * PlotFlagAddEvent: Called when a Flag is added to a plot.
     *
     * @param flag Flag that was added
     * @param plot Plot to which the flag was added
     */
    public PlotFlagAddEvent(PlotFlag<?, ?> flag, Plot plot) {
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
