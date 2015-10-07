package com.plotsquared.sponge.events;

import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;

import com.intellectualcrafters.plot.object.Plot;

public class PlayerPlotDeniedEvent extends PlotEvent {
    
    private final Player initiator;
    private final boolean added;
    private final UUID player;
    
    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a plot
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was denied/un-denied
     * @param added     true of add to deny list, false if removed
     */
    public PlayerPlotDeniedEvent(final Player initiator, final Plot plot, final UUID player, final boolean added) {
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
