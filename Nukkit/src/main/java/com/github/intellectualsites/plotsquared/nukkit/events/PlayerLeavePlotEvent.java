package com.github.intellectualsites.plotsquared.nukkit.events;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

/**


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
        this.player = player;
        this.plot = plot;
    }

    public static HandlerList getHandlers() {
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

}
