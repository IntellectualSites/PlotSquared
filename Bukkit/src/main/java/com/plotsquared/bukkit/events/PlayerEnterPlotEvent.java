package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerEnterPlotEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Plot plot;

    /**
     * Called when a player leaves a plot.
     *
     * @param player Player that entered the plot
     * @param plot   Plot that was entered
     */
    public PlayerEnterPlotEvent(Player player, Plot plot) {
        super(player);
        this.plot = plot;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plot involved.
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
