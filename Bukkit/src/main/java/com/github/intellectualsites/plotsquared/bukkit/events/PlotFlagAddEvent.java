package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a Flag is added to a plot.
 */
public class PlotFlagAddEvent extends PlotFlagEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    /**
     * PlotFlagAddEvent: Called when a Flag is added to a plot.
     *
     * @param flag Flag that was added
     * @param plot Plot to which the flag was added
     */
    public PlotFlagAddEvent(PlotFlag<?, ?> flag, Plot plot) {
        super(plot, flag);
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
