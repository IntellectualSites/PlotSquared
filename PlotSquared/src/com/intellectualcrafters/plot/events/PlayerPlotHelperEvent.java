package com.intellectualcrafters.plot.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.Plot;

/**
 * Created by Citymonstret on 2014-08-16.
 */
public class PlayerPlotHelperEvent extends Event {
    private static HandlerList handlers = new HandlerList();

    private Plot plot;
    private Player initiator;
    private boolean added;
    private UUID player;
    /**
     * PlayerPlotHelperEvent: Called when a plot helper is added/removed
     * @param initiator
     * @param plot
     * @param player
     * @param added
     */
    public PlayerPlotHelperEvent(Player initiator, Plot plot, UUID player, boolean added) {
        this.initiator = initiator;
        this.plot = plot;
        this.added = added;
        this.player = player;
    }
    
    /**
     * If a player was added 
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }
    /**
     * The UUID added/removed
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }
    /**
     * The plot involved
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }
    /**
     * The player initiating the action
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
