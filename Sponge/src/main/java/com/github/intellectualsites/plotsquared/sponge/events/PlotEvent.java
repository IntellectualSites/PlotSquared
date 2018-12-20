package com.github.intellectualsites.plotsquared.sponge.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class PlotEvent extends AbstractEvent {

    private final Plot plot;

    public PlotEvent(final Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return plot;
    }

    @Override public Cause getCause() {
        return null;
    }

}
