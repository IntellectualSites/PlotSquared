package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;

/**
 * Called after a plot move was performed and succeeded.
 *
 * @see com.plotsquared.core.events.PlotMoveEvent
 * @since TODO
 */
public class PostPlotMoveEvent extends PlotEvent {

    private final PlotPlayer<?> initiator;
    private final PlotId oldPlot;

    public PostPlotMoveEvent(final PlotPlayer<?> initiator, final PlotId oldPlot, final Plot plot) {
        super(plot);
        this.initiator = initiator;
        this.oldPlot = oldPlot;
    }

    public PlotPlayer<?> initiator() {
        return initiator;
    }

    public PlotId oldPlot() {
        return oldPlot;
    }

}
