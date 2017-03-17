package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ArrayUtil;

public class SinglePlotAreaManager extends DefaultPlotAreaManager {
    private final SinglePlotArea area;
    private final SinglePlotArea[] array;
    private PlotArea[] all;

    public SinglePlotAreaManager(SinglePlotArea area) {
        this.area = area;
        this.array = new SinglePlotArea[] { area };
        this.all = new PlotArea[] { area };
    }

    @Override
    public PlotArea getApplicablePlotArea(Location location) {
        PlotArea found = super.getApplicablePlotArea(location);
        return found != null ? found : area;
    }

    @Override
    public PlotArea getPlotArea(Location location) {
        PlotArea found = super.getPlotArea(location);
        return found != null ? found : area;
    }

    @Override
    public PlotArea getPlotArea(String world, String id) {
        PlotArea found = super.getPlotArea(world, id);
        return found != null ? found : area;
    }

    @Override
    public PlotArea[] getPlotAreas(String world, RegionWrapper region) {
        PlotArea[] found = super.getPlotAreas(world, region);
        return found != null ? found : array;
    }

    @Override
    public PlotArea[] getAllPlotAreas() {
        return all;
    }

    @Override
    public String[] getAllWorlds() {
        return super.getAllWorlds();
    }

    @Override
    public void addPlotArea(PlotArea area) {
        super.addPlotArea(area);
        all = ArrayUtil.concatAll(super.getAllPlotAreas(), array);
    }

    @Override
    public void removePlotArea(PlotArea area) {
        if (area == this.area) {
            throw new UnsupportedOperationException("Cannot remove base area!");
        }
        super.removePlotArea(area);
    }

    @Override
    public void addWorld(String worldName) {
        super.addWorld(worldName);
    }

    @Override
    public void removeWorld(String worldName) {
        super.removeWorld(worldName);
    }

}
