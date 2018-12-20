package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class PlotUnlinkEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<PlotId> plots;
    private final World world;
    private final PlotArea area;
    private boolean cancelled;

    /**
     * Called when a mega-plot is unlinked.
     *
     * @param world World in which the event occurred
     * @param plots Plots that are involved in the event
     */
    public PlotUnlinkEvent(World world, PlotArea area, ArrayList<PlotId> plots) {
        this.plots = plots;
        this.world = world;
        this.area = area;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plots involved.
     *
     * @return The {@link PlotId}'s of the plots involved
     */
    public ArrayList<PlotId> getPlots() {
        return this.plots;
    }

    public World getWorld() {
        return this.world;
    }

    public PlotArea getArea() {
        return this.area;
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
