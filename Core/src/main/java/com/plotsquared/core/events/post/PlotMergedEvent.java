package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotPlayerEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

/**
 * Called after multiple plots were merged with another.
 */
public class PlotMergedEvent extends PlotPlayerEvent {

    /**
     * Instantiate a new PlotMergedEvent.
     *
     * @param plotPlayer The {@link PlotPlayer} that initiated the merge.
     * @param plot       The final merged plot.
     */
    public PlotMergedEvent(final PlotPlayer<?> plotPlayer, final Plot plot) {
        super(plotPlayer, plot);
    }

}
