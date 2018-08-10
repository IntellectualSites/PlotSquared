package com.github.intellectualsites.plotsquared.nukkit.events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;

/**
 * Called when a plot is cleared
 */
public class PlotClearEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    public PlotClearEvent(Plot plot) {
        super(plot);
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get the PlotId.
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name.
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }


    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
