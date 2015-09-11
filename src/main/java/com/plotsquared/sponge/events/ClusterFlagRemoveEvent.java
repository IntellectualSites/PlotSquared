package com.plotsquared.sponge.events;

import org.spongepowered.api.event.AbstractEvent;
import org.spongepowered.api.event.Cancellable;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.PlotCluster;

public class ClusterFlagRemoveEvent extends AbstractEvent implements Cancellable
{

    private final PlotCluster cluster;
    private final Flag flag;
    private boolean cancelled;

    public ClusterFlagRemoveEvent(final Flag flag, final PlotCluster cluster)
    {
        this.cluster = cluster;
        this.flag = flag;
    }

    /**
     * Get the cluster involved
     *
     * @return PlotCluster
     */
    public PlotCluster getCluster()
    {
        return cluster;
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
