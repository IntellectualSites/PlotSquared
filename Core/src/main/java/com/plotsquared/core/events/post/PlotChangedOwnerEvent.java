package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotPlayerEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Called after the owner of a plot was updated.
 */
public class PlotChangedOwnerEvent extends PlotPlayerEvent {

    @Nullable
    private final UUID oldOwner;

    /**
     * Instantiate a new PlotChangedOwnerEvent.
     *
     * @param initiator The player who executed the owner change.
     * @param plot      The plot which owner was changed.
     * @param oldOwner  The previous owner - if present, otherwise {@code null}.
     */
    public PlotChangedOwnerEvent(final PlotPlayer<?> initiator, final Plot plot, @Nullable UUID oldOwner) {
        super(initiator, plot);
        this.oldOwner = oldOwner;
    }

    /**
     * @return the old owner of the plot - if present, otherwise {@code null}.
     */
    public @Nullable UUID getOldOwner() {
        return oldOwner;
    }

    /**
     * @return {@code true} if the plot had an owner, {@code false} otherwise.
     * @see #getOldOwner()
     */
    public boolean hasOldOwner() {
        return getOldOwner() != null;
    }

    /**
     * @return {@code true} if the plot now has an owner, {@code false} otherwise.
     * @see Plot#hasOwner()
     */
    public boolean hasNewOwner() {
        return getPlot().hasOwner();
    }

}
