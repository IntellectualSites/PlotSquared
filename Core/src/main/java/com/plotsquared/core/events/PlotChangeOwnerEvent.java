package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.player.PlotPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlotChangeOwnerEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotPlayer initiator;
    @Nullable private final UUID oldOwner;
    @Nullable private UUID newOwner;
    private boolean hasOldOwner;
    private Result eventResult;

    /**
     * PlotChangeOwnerEvent: Called when a plot's owner is change.
     *
     * @param initiator   The player attempting to set the plot's owner
     * @param plot        The plot having its owner changed
     * @param oldOwner    The old owner of the plot or null
     * @param newOwner    The new owner of the plot or null
     * @param hasOldOwner If the plot has an old owner
     */
    public PlotChangeOwnerEvent(PlotPlayer initiator, Plot plot, @Nullable UUID oldOwner,
        @Nullable UUID newOwner, boolean hasOldOwner) {
        super(plot);
        this.initiator = initiator;
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
        this.hasOldOwner = hasOldOwner;
    }

    /**
     * Get the PlotId.
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name.
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }

    /**
     * Get the change-owner initator
     *
     * @return Player
     */
    public PlotPlayer getInitiator() {
        return this.initiator;
    }

    /**
     * Get the old owner of the plot. Null if not exists.
     *
     * @return UUID
     */
    @Nullable public UUID getOldOwner() {
        return this.oldOwner;
    }

    /**
     * Get the new owner of the plot
     *
     * @return UUID
     */
    @Nullable public UUID getNewOwner() {
        return this.newOwner;
    }


    /**
     * Set the new owner of the plot. Null for no owner.
     *
     * @param newOwner the new owner or null
     */
    public void setNewOwner(@Nullable UUID newOwner) {
        this.newOwner = newOwner;
    }

    /**
     * Get if the plot had an old owner
     *
     * @return boolean
     */
    public boolean hasOldOwner() {
        return this.hasOldOwner;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
