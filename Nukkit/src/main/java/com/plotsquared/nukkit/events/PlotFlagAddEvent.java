package com.plotsquared.nukkit.events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;

/**
 * Called when a Flag is added to a plot.
 */
public class PlotFlagAddEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagAddEvent: Called when a Flag is added to a plot.
     *
     * @param flag Flag that was added
     * @param plot Plot to which the flag was added
     */
    public PlotFlagAddEvent(Flag flag, Plot plot) {
        super(plot);
        this.flag = flag;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get the flag involved.
     *
     * @return Flag
     */
    public Flag getFlag() {
        return this.flag;
    }

    @Override public final boolean isCancelled() {
        return this.cancelled;
    }

    @Override public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
