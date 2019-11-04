package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a flag is removed from a plot
 */
public class PlotFlagRemoveEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagRemoveEvent: Called when a flag is removed from a plot
     *
     * @param flag Flag that was removed
     * @param plot Plot from which the flag was removed
     */
    public PlotFlagRemoveEvent(Flag flag, Plot plot) {
        super(plot);
        this.flag = flag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the flag involved
     *
     * @return Flag
     */
    public Flag getFlag() {
        return this.flag;
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }

    @Override public final boolean isCancelled() {
        return this.cancelled;
    }

    @Override public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
