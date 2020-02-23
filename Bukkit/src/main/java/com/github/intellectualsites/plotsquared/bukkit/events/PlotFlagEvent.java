package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

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
