package com.plotsquared.sponge.events;

import java.util.ArrayList;

import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

public class PlotMergeEvent extends AbstractEvent implements Cancellable {
    private final ArrayList<PlotId> plots;
    private boolean cancelled;
    private Plot plot;
    private World world;
    
    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotMergeEvent(final World world, final Plot plot, final ArrayList<PlotId> plots) {
        this.plots = plots;
    }
    
    /**
     * Get the plots being added;
     *
     * @return Plot
     */
    public ArrayList<PlotId> getPlots() {
        return plots;
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
}
