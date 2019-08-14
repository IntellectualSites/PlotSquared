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
 * Event called when plots are automatically merged with /plot auto
 * {@inheritDoc}
 */
public final class PlotAutoMergeEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final List<PlotId> plots;
    @Getter private final World world;
    @Getter @Setter private boolean cancelled;

    /**
     * PlotAutoMergeEvent: Called when plots are automatically merged with /plot auto
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotAutoMergeEvent(@NotNull final World world, @NotNull final Plot plot,
        @NotNull final List<PlotId> plots) {
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
