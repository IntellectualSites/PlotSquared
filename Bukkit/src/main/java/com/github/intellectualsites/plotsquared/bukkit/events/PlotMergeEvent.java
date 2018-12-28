package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final List<PlotId> plots;
    @Getter private final World world;
    @Getter @Setter private boolean cancelled;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotMergeEvent(@Nonnull final World world, @Nonnull final Plot plot,
        @Nonnull final List<PlotId> plots) {
        super(plot);
        this.world = world;
        this.plots = plots;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plots being added.
     *
     * @return Unmodifiable list containing the merging plots
     */
    public List<PlotId> getPlots() {
        return Collections.unmodifiableList(this.plots);
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
