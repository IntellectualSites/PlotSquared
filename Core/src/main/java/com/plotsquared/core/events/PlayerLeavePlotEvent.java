package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;

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
