package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.plot.Plot;

/**
 * Called after a {@link Plot} was deleted.
 */
public class PlotDeletedEvent extends PlotEvent {

    /**
     * Instantiate a new PlotDeleteEvent.
     *
     * @param plot The plot which was deleted.
     */
    public PlotDeletedEvent(final Plot plot) {
        super(plot);
    }

}
