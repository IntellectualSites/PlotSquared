package com.plotsquared.sponge.events;

import java.util.UUID;

import org.spongepowered.api.entity.player.Player;

import com.intellectualcrafters.plot.object.Plot;

public class PlayerPlotHelperEvent extends PlotEvent {
    
    private final Player initiator;
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
    public PlayerPlotHelperEvent(final Player initiator, final Plot plot, final UUID player, final boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }
    
    /**
     * If a user was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return added;
    }
    
    /**
     * The player added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return player;
    }
    
    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return initiator;
    }
}
