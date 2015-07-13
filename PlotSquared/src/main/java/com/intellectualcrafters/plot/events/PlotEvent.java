package com.intellectualcrafters.plot.events;

import com.intellectualcrafters.plot.object.Plot;
import com.sk89q.worldedit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class PlotEvent extends Event {

    private final Plot plot;

    public PlotEvent(final Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

}
