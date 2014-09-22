package com.intellectualcrafters.plot.events;

import com.intellectualcrafters.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-08-16.
 */
public class PlayerPlotHelperEvent extends Event {
    private static HandlerList handlers = new HandlerList();

    private Plot plot;
    private Player initiator;
    private boolean added;
    private UUID player;

    public PlayerPlotHelperEvent(Player initiator, Plot plot, UUID player, boolean added) {
        this.initiator = initiator;
        this.plot = plot;
        this.added = added;
        this.player = player;
    }

    public boolean wasAdded() {
        return this.added;
    }

    public UUID getPlayer() {
        return this.player;
    }

    public Plot getPlot() {
        return this.plot;
    }

    public Player getInitiator() {
        return this.initiator;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
