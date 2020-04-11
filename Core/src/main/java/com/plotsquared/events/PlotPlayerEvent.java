package com.plotsquared.events;

import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;

public abstract class PlotPlayerEvent extends PlotEvent {

    private final PlotPlayer plotPlayer;

    public PlotPlayerEvent(PlotPlayer plotPlayer, Plot plot) {
        super(plot);
        this.plotPlayer = plotPlayer;
    }

    public PlotPlayer getPlotPlayer() {
        return this.plotPlayer;
    }

}
