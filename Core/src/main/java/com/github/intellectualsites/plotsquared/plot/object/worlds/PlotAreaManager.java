package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;

public interface PlotAreaManager {
    PlotArea getApplicablePlotArea(Location location);

    PlotArea getPlotArea(Location location);

    PlotArea getPlotArea(String world, String id);

    PlotArea[] getPlotAreas(String world, RegionWrapper region);

    PlotArea[] getAllPlotAreas();

    String[] getAllWorlds();

    void addPlotArea(PlotArea area);

    void removePlotArea(PlotArea area);

    void addWorld(String worldName);

    void removeWorld(String worldName);

}
