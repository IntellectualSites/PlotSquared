package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

public class PlayerClaimPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final boolean auto;
    private Result eventResult;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player Player that claimed the plot
     * @param plot   Plot that was claimed
     */
    public PlayerClaimPlotEvent(PlotPlayer player, Plot plot, boolean auto) {
        super(player, plot);
        this.auto = auto;
    }

    /**
     * @return true if it was an automated claim, else false
     */
    public boolean wasAuto() {
        return this.auto;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
