package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;

public class PlayerEnterPlotEvent extends PlotPlayerEvent {

    /**
     * Called when a player leaves a plot.
     *
     * @param player Player that entered the plot
     * @param plot   Plot that was entered
     */
    public PlayerEnterPlotEvent(PlotPlayer player, Plot plot) {
        super(player, plot);
    }
}
