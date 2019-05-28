package com.plotsquared.nukkit.events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Level;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

import java.util.ArrayList;

public class PlotAutoMergeEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<PlotId> plots;
    private final Level world;
    private boolean cancelled;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotAutoMergeEvent(Level world, Plot plot, ArrayList<PlotId> plots) {
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

    public Level getWorld() {
        return this.world;
    }

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
