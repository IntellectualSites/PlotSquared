package com.plotsquared.sponge.events;

import org.spongepowered.api.event.AbstractEvent;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotDeleteEvent extends AbstractEvent
{
    private final PlotId id;
    private final String world;

    /**
     * PlotDeleteEvent: Called when a plot is deleted
     *
     * @param world The world in which the plot was deleted
     * @param id    The ID of the plot that was deleted
     */
    public PlotDeleteEvent(final String world, final PlotId id)
    {
        this.id = id;
        this.world = world;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId()
    {
        return id;
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld()
    {
        return world;
    }
}
