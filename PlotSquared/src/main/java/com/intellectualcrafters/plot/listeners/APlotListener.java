package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class APlotListener {
    
    public static APlotListener manager;
    
    public abstract void plotExit(final PlotPlayer player, final Plot plot);
    
    public abstract void plotEntry(final PlotPlayer player, final Plot plot);
}
