package com.plotsquared.sponge.events;

import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;

import com.intellectualcrafters.plot.object.Plot;

public class PlayerPlotTrustedEvent extends PlotEvent {

    private final Player initiator;
    private final boolean added;
    private final UUID player;
    
    /**
     * PlayerPlotTrustedEvent: Called when a plot trusted user is added/removed
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was added/removed from the trusted list
     * @param added     true of the player was added, false if the player was removed
     */
    public PlayerPlotTrustedEvent(final Player initiator, final Plot plot, final UUID player, final boolean added) {
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
        return this.added;
    }

    /**
     * The player added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }
 }