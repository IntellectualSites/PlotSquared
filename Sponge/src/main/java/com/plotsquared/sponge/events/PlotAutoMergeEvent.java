package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import java.util.ArrayList;

public class PlotAutoMergeEvent extends AbstractEvent implements Cancellable {

    private final ArrayList<PlotId> plots;
    private final World world;
    private Plot plot;
    private boolean cancelled;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotAutoMergeEvent(World world, Plot plot, ArrayList<PlotId> plots) {
        this.world = world;
        this.plots = plots;
        this.plot = plot;
    }

    /**
     * Get the plots being added.
     *
     * @return Plot
     */
    public ArrayList<PlotId> getPlots() {
        return this.plots;
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
        return this.world;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public Cause getCause() {
        return null;
    }
}
