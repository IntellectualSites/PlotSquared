package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.bukkit.event.HandlerList;

/**
 * Called when a plot component is set
 */
public class PlotComponentSetEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String component;

    public PlotComponentSetEvent(Plot plot, String component) {
        super(plot);
        this.component = component;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }

    /**
     * Get the component which was set
     *
     * @return Component name
     */
    public String getComponent() {
        return this.component;
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
