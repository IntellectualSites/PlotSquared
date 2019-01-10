package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final int dir;
    @Getter private final int max;
    @Getter private final World world;
    @Getter @Setter private boolean cancelled;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param dir   The direction of the merge
     * @param max   Max merge size
     */
    public PlotMergeEvent(@Nonnull final World world, @Nonnull final Plot plot,
        @Nonnull final int dir, @Nonnull final int max) {
        super(plot);
        this.world = world;
        this.dir = dir;
        this.max = max;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
