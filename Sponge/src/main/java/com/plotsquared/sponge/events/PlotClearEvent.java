package com.plotsquared.sponge.events;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotClearEvent extends AbstractEvent implements Cancellable {
    private final PlotId id;
    private final String world;
    private boolean cancelled;
    
    /**
     * PlotDeleteEvent: Called when a plot is cleared
     *
     * @param world The world in which the plot was cleared
     * @param id    The plot that was cleared
     */
    public PlotClearEvent(final String world, final PlotId id) {
        this.id = id;
        this.world = world;
    }
    
    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return id;
    }
    
    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return world;
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
