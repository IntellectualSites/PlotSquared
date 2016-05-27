package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotComponentSetEvent extends AbstractEvent {
    private final Plot plot;
    private final String component;

    /**
     * PlotDeleteEvent: Called when a plot is deleted
     *
     * @param plot    The plot that was deleted
     */
    public PlotComponentSetEvent(Plot plot, String component) {
        this.plot = plot;
        this.component = component;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return this.plot.getId();
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return this.plot.getArea().worldname;
    }

    /**
     * Get the component which was set
     *
     * @return Component name
     */
    public String getComponent() {
        return this.component;
    }

    @Override
    public Cause getCause() {
        return null;
    }
}
