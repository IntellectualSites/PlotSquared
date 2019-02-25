package com.plotsquared.sponge.events;

import java.util.ArrayList;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

public class PlotMergeEvent extends AbstractEvent implements Cancellable {
    private boolean cancelled;
    private Plot plot;
    private final int dir;
    private final int max;
    private World world;
    
    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param dir   The direction of the merge
     * @param max   Max merge size
     */
    public PlotMergeEvent(World world, Plot plot, final int dir, final int max) {
        this.world = world;
        this.dir = dir;
        this.max = max;
        this.plot = plot;
    }

    public int getDir() {
        return this.dir;
    }

    public int getMax() {
        return this.max;
    }
    
    /**
     * Get the main plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        return plot;
    }
    
    public World getWorld() {
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
