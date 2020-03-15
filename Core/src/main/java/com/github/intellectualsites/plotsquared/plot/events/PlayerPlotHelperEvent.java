package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

import java.util.UUID;

/**
 *
 */
public class PlayerPlotHelperEvent extends PlotEvent {

    private final PlotPlayer initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotHelperEvent: Called when a plot helper is added/removed
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was added/removed from the helper list
     * @param added     true of the player was added, false if the player was removed
     */
    public PlayerPlotHelperEvent(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    /**
     * If a player was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The UUID added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action
     *
     * @return PlotPlayer
     */
    public PlotPlayer getInitiator() {
        return this.initiator;
    }

}
