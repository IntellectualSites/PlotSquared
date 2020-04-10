package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

/**
 *
 */
public class PlayerLeavePlotEvent extends PlotPlayerEvent {

    /**
     * PlayerLeavePlotEvent: Called when a player leaves a plot
     *
     * @param player Player that left the plot
     * @param plot   Plot that was left
     */
    public PlayerLeavePlotEvent(PlotPlayer player, Plot plot) {
        super(player, plot);
    }
}
