package com.github.intellectualsites.plotsquared.nukkit.events;

import cn.nukkit.event.Event;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

public abstract class PlotEvent extends Event {

    private final Plot plot;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

}
