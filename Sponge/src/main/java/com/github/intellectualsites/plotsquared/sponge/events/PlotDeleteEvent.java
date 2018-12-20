package com.github.intellectualsites.plotsquared.sponge.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlotDeleteEvent extends AbstractEvent {
    private final Plot plot;

    /**
     * PlotDeleteEvent: Called when a plot is deleted
     *
     * @param plot The plot that was deleted
     */
    public PlotDeleteEvent(Plot plot) {
        this.plot = plot;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return plot.getId();
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return plot.getWorldName();
    }

    @Override public Cause getCause() {
        return null;
    }
}
