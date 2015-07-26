package com.plotsquared.bukkit.events;

import org.bukkit.event.Event;

import com.intellectualcrafters.plot.object.Plot;

public abstract class PlotEvent extends Event {

    private final Plot plot;

    public PlotEvent(final Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

}
