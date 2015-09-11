package com.plotsquared.sponge.events;

import org.spongepowered.api.event.AbstractEvent;

import com.intellectualcrafters.plot.object.Plot;

public abstract class PlotEvent extends AbstractEvent
{

    private final Plot plot;

    public PlotEvent(final Plot plot)
    {
        this.plot = plot;
    }

    public final Plot getPlot()
    {
        return plot;
    }

}
