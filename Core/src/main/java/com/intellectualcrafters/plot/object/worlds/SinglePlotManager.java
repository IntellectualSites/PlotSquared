package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import java.util.ArrayList;

public class SinglePlotManager extends PlotManager {
    @Override
    public PlotId getPlotIdAbs(PlotArea plotArea, int x, int y, int z) {
        return null;
    }

    @Override
    public PlotId getPlotId(PlotArea plotArea, int x, int y, int z) {
        return null;
    }

    @Override
    public Location getPlotBottomLocAbs(PlotArea plotArea, PlotId plotId) {
        return null;
    }

    @Override
    public Location getPlotTopLocAbs(PlotArea plotArea, PlotId plotId) {
        return null;
    }

    @Override
    public boolean clearPlot(PlotArea plotArea, Plot plot, Runnable whenDone) {
        return false;
    }

    @Override
    public boolean claimPlot(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean unclaimPlot(PlotArea plotArea, Plot plot, Runnable whenDone) {
        return false;
    }

    @Override
    public Location getSignLoc(PlotArea plotArea, Plot plot) {
        return null;
    }

    @Override
    public String[] getPlotComponents(PlotArea plotArea, PlotId plotId) {
        return new String[0];
    }

    @Override
    public boolean setComponent(PlotArea plotArea, PlotId plotId, String component, PlotBlock[] blocks) {
        return false;
    }

    @Override
    public boolean createRoadEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean createRoadSouth(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean createRoadSouthEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean removeRoadEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean removeRoadSouth(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean removeRoadSouthEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override
    public boolean startPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override
    public boolean startPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override
    public boolean finishPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override
    public boolean finishPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }
}
