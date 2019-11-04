package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.event.Event;

public abstract class PlotEvent extends Event {

    private final Plot plot;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

}
