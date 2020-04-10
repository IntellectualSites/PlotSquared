package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

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
