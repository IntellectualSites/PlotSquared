package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotDeleteEvent extends AbstractEvent implements Cancellable {
    private final Plot plot;
    private boolean cancelled;

    /**
     * PlotDeleteEvent: Called when a plot is deleted
     *
     * @param plot    The plot that was deleted
     */
    public PlotDeleteEvent(Plot plot) {
        this.plot = plot;
    }
    
    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return plot.getId();
    }
    
    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return plot.getWorldName();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return null;
    }
}
