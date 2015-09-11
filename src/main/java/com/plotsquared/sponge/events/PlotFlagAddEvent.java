package com.plotsquared.sponge.events;

import org.spongepowered.api.event.Cancellable;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;

public class PlotFlagAddEvent extends PlotEvent implements Cancellable
{
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagAddEvent: Called when a Flag is added to a plot
     *
     * @param flag Flag that was added
     * @param plot Plot to which the flag was added
     */
    public PlotFlagAddEvent(final Flag flag, final Plot plot)
    {
        super(plot);
        this.flag = flag;
    }

    /**
     * Get the flag involved
     *
     * @return Flag
     */
    public Flag getFlag()
    {
        return flag;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel)
    {
        cancelled = cancel;
    }
}
