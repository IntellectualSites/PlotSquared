package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.plot.Plot;

/**
 * Called when several plots were unlinked.
 */
public class PlotUnlinkedEvent extends PlotEvent {

    private final PlotUnlinkEvent.REASON reason;

    /**
     * Instantiate a new PlotUnlinkedEvent.
     *
     * @param plot   The unlinked plot.
     * @param reason The reason for the unlink.
     */
    public PlotUnlinkedEvent(final Plot plot, PlotUnlinkEvent.REASON reason) {
        super(plot);
        this.reason = reason;
    }

    /**
     * The original reason provided by {@link PlotUnlinkEvent}.
     *
     * @return The reason for the unlink.
     */
    public PlotUnlinkEvent.REASON getReason() {
        return reason;
    }

}
