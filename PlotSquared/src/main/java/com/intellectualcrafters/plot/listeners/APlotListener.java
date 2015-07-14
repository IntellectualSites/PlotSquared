package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class APlotListener {
    
    public static APlotListener manager;
    
    public abstract boolean plotEntry(final PlotPlayer player, final Plot plot);
    
    public abstract boolean plotExit(final PlotPlayer player, final Plot plot);
}
