package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.bukkit.event.HandlerList;

/**
 * Called when a plot is deleted
 */
public class PlotDeleteEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();

    public PlotDeleteEvent(Plot plot) {
        super(plot);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
