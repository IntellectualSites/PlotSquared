package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.event.Event;

public abstract class PlotEvent extends Event {

    private final Plot plot;

    public PlotEvent(final Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

}
