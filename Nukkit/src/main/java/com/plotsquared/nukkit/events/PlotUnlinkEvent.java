package com.plotsquared.nukkit.events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Level;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import java.util.ArrayList;

public class PlotUnlinkEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<PlotId> plots;
    private final Level world;
    private final PlotArea area;
    private boolean cancelled;

    /**
     * Called when a mega-plot is unlinked.
     *
     * @param world World in which the event occurred
     * @param plots Plots that are involved in the event
     */
    public PlotUnlinkEvent(Level world, PlotArea area, ArrayList<PlotId> plots) {
        this.plots = plots;
        this.world = world;
        this.area = area;
    }

    public static HandlerList getHandlers() {
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

    public Level getLevel() {
        return this.world;
    }

    public PlotArea getArea() {
        return this.area;
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
