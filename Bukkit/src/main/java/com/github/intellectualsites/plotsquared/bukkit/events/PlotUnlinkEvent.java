package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Event called when several merged plots are unlinked
 * {@inheritDoc}
 */
public final class PlotUnlinkEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final List<PlotId> plots;
    @Getter private final World world;
    @Getter private final PlotArea area;
    @Getter @Setter private boolean cancelled;

    /**
     * Called when a mega-plot is unlinked.
     *
     * @param world World in which the event occurred
     * @param plots Plots that are involved in the event
     */
    public PlotUnlinkEvent(@NotNull final World world, @NotNull final PlotArea area,
        @NotNull final List<PlotId> plots) {
        this.plots = plots;
        this.world = world;
        this.area = area;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plots involved.
     *
     * @return Unmodifiable list containing {@link PlotId PlotIds} of the plots involved
     */
    public List<PlotId> getPlots() {
        return Collections.unmodifiableList(this.plots);
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
