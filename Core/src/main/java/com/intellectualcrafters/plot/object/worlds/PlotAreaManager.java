package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RegionWrapper;

public interface PlotAreaManager {
    public PlotArea getApplicablePlotArea(Location location);
    public PlotArea getPlotArea(Location location);
    public PlotArea getPlotArea(String world, String id);
    public PlotArea[] getPlotAreas(String world, RegionWrapper region);
    public PlotArea[] getAllPlotAreas();
    public String[] getAllWorlds();
    public void addPlotArea(PlotArea area);
    public void removePlotArea(PlotArea area);
    public void addWorld(String worldName);
    public void removeWorld(String worldName);

}
