package com.plotsquared.events;

import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.plot.Plot;

public abstract class PlotFlagEvent extends PlotEvent {
    private final PlotFlag<?, ?> flag;

    protected PlotFlagEvent(Plot plot, PlotFlag<?, ?> flag) {
        super(plot);
        this.flag = flag;
    }

    /**
     * Get the flag involved
     *
     * @return the flag involved 
     */
    public PlotFlag<?, ?> getFlag() {
        return flag;
    }
}
