package com.plotsquared.events;

import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;

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
