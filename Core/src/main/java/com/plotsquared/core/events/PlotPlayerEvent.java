package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;

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
