package com.plotsquared.sponge.events;

import org.spongepowered.api.event.Cancellable;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;

public class PlotFlagRemoveEvent extends PlotEvent implements Cancellable {
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagRemoveEvent: Called when a flag is removed from a plot
     *
     * @param flag Flag that was removed
     * @param plot Plot from which the flag was removed
     */
    public PlotFlagRemoveEvent(final Flag flag, final Plot plot) {
        super(plot);
        this.flag = flag;
    }
    
    /**
     * Get the flag involved
     *
     * @return Flag
     */
    public Flag getFlag() {
        return this.flag;
    }

    @Override
    public boolean isCancelled() {
       return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
       cancelled = cancel;
    }
 }