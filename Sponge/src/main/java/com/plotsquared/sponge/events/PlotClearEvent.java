package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlotClearEvent extends AbstractEvent implements Cancellable {

    private final Plot plot;
    private boolean cancelled;

    /**
     * PlotDeleteEvent: Called when a plot is cleared
     *
     * @param plot The plot that was cleared
     */

    public PlotClearEvent(Plot plot) {
        this.plot = plot;
    }

    /**
     * Get the PlotId.
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return this.plot.getId();
    }

    /**
     * Get the world name.
     *
     * @return String
     */
    public String getWorld() {
        return this.plot.getWorldName();
    }

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override public Cause getCause() {
        return null;
    }
}
