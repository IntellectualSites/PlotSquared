package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.sk89q.worldedit.regions.CuboidRegion;

public interface PlotAreaManager {
    PlotArea getApplicablePlotArea(Location location);

    PlotArea getPlotArea(Location location);

    PlotArea getPlotArea(String world, String id);

    PlotArea[] getPlotAreas(String world, CuboidRegion region);

    PlotArea[] getAllPlotAreas();

    String[] getAllWorlds();

    void addPlotArea(PlotArea area);

    void removePlotArea(PlotArea area);

    void addWorld(String worldName);

    void removeWorld(String worldName);

}
