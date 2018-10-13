package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;

import java.util.UUID;

public class PlotChangeOwnerEvent extends PlotEvent implements Cancellable {

    private final Player initiator;
    private final UUID newOwner;
    private final UUID oldOwner;
    private final boolean hasOldOwner;
    private boolean cancelled;

    /**
     * PlotChangeOwnerEvent: Called when a plot's owner is change.
     *
     * @param newOwner The new owner of the plot
     * @param oldOwner The old owner of the plot
     * @param plot     The plot having its owner changed
     */
    public PlotChangeOwnerEvent(Player initiator, Plot plot, UUID oldOwner, UUID newOwner, boolean hasOldOwner) {
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
    public Player getInitiator() {
        return this.initiator;
    }

    /**
     * Get the old owner of the plot. Null if not exists.
     *
     * @return UUID
     */
    public UUID getOldOwner() {
        return this.oldOwner;
    }

    /**
     * Get the new owner of the plot
     *
     * @return UUID
     */
    public UUID getNewOwner() {
        return this.newOwner;
    }

    /**
     * Get if the plot had an old owner
     *
     * @return boolean
     */
    public boolean hasOldOwner() {
        return this.hasOldOwner;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
