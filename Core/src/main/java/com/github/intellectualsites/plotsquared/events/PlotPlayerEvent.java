package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

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
