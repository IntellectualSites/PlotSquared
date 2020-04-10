package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

import java.util.UUID;

public class PlayerPlotDeniedEvent extends PlotEvent {

    private final PlotPlayer initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a plot.
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was denied/un-denied
     * @param added     true of add to deny list, false if removed
     */
    public PlayerPlotDeniedEvent(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    /**
     * If a user was added.
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The player added/removed.
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action.
     *
     * @return PlotPlayer
     */
    public PlotPlayer getInitiator() {
        return this.initiator;
    }
}
