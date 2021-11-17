package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotPlayerEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

/**
 * Called after a plot was chosen for a player by `/plot auto`
 * <br>
 * Called after {@link com.plotsquared.core.events.PlayerAutoPlotEvent}  and only, if no listener cancelled the action.
 */
public class PlayerAutoPlotPostEvent extends PlotPlayerEvent {

    /**
     * Instantiate a new PlayerAutoPlotPostEvent.
     *
     * @param plotPlayer The player who claims a new plot by `/plot auto`.
     * @param plot       The plot that is assigned to the player.
     */
    public PlayerAutoPlotPostEvent(final PlotPlayer<?> plotPlayer, final Plot plot) {
        super(plotPlayer, plot);
    }

}
