package com.intellectualcrafters.plot.events;

import com.intellectualcrafters.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Created by Citymonstret on 2014-08-16.
 */
public class PlayerEnterPlotEvent extends PlayerEvent {

    private static HandlerList handlers = new HandlerList();

    private Plot plot;

    public PlayerEnterPlotEvent(Player player, Plot plot) {
        super(player);
        this.plot = plot;
    }

    public Plot getPlot() {
        return this.plot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}

