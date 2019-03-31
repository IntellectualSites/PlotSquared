package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;

public class DelegatePlotAreaManager implements PlotAreaManager {
    @Override
    public PlotArea getApplicablePlotArea(Location location) {
        return parent.getApplicablePlotArea(location);
    }

    @Override
    public PlotArea getPlotArea(Location location) {
        return parent.getPlotArea(location);
    }

    @Override
    public PlotArea getPlotArea(String world, String id) {
        return parent.getPlotArea(world, id);
    }

    @Override
    public PlotArea[] getPlotAreas(String world, RegionWrapper region) {
        return parent.getPlotAreas(world, region);
    }

    @Override
    public PlotArea[] getAllPlotAreas() {
        return parent.getAllPlotAreas();
    }

    @Override
    public String[] getAllWorlds() {
        return parent.getAllWorlds();
    }

    @Override
    public void addPlotArea(PlotArea area) {
        parent.addPlotArea(area);
    }

    @Override
    public void removePlotArea(PlotArea area) {
        parent.removePlotArea(area);
    }

    @Override
    public void addWorld(String worldName) {
        parent.addWorld(worldName);
    }

    @Override
    public void removeWorld(String worldName) {
        parent.removeWorld(worldName);
    }

    private final PlotAreaManager parent;

    public DelegatePlotAreaManager(PlotAreaManager parent) {
        this.parent = parent;
    }
}
