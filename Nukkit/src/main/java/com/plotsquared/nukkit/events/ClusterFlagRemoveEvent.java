package com.plotsquared.nukkit.events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.PlotCluster;

/**
 * Called when a flag is removed from a plot.
 */
public class ClusterFlagRemoveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PlotCluster cluster;
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagRemoveEvent: Called when a flag is removed from a plot.
     *
     * @param flag    Flag that was removed
     * @param cluster PlotCluster from which the flag was removed
     */
    public ClusterFlagRemoveEvent(Flag flag, PlotCluster cluster) {
        this.cluster = cluster;
        this.flag = flag;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get the cluster involved.
     *
     * @return PlotCluster
     */
    public PlotCluster getCluster() {
        return this.cluster;
    }

    /**
     * Get the flag involved.
     *
     * @return Flag
     */
    public Flag getFlag() {
        return this.flag;
    }

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
