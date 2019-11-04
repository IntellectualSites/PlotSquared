package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 */
public class PlayerLeavePlotEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Plot plot;

    /**
     * PlayerLeavePlotEvent: Called when a player leaves a plot
     *
     * @param player Player that left the plot
     * @param plot   Plot that was left
     */
    public PlayerLeavePlotEvent(Player player, Plot plot) {
        super(player);
        this.plot = plot;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plot involved
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
