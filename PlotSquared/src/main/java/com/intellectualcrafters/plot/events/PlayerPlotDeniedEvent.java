package com.intellectualcrafters.plot.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.Plot;

/**
 * Created by Citymonstret on 2014-08-16.
 */
public class PlayerPlotDeniedEvent extends Event {
    private static HandlerList handlers = new HandlerList();

    private final Plot         plot;
    private final Player       initiator;
    private final boolean      added;
    private final UUID         player;

    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a
     * plot
     *
     * @param initiator
     * @param plot
     * @param player
     * @param added
     */
    public PlayerPlotDeniedEvent(final Player initiator, final Plot plot, final UUID player, final boolean added) {
        this.initiator = initiator;
        this.plot = plot;
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
     * The plot involved
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
