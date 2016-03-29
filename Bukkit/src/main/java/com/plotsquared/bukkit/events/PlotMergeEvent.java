package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class PlotMergeEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<PlotId> plots;
    private final World world;
    private boolean cancelled;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotMergeEvent(World world, Plot plot, ArrayList<PlotId> plots) {
        super(plot);
        this.world = world;
        this.plots = plots;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plots being added.
     *
     * @return Plot
     */
    public ArrayList<PlotId> getPlots() {
        return this.plots;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
