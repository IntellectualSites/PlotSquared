package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

import java.io.File;
import java.util.ArrayList;

public class SinglePlotManager extends PlotManager {
    @Override public PlotId getPlotIdAbs(PlotArea plotArea, int x, int y, int z) {
        return new PlotId(0, 0);
    }

    @Override public PlotId getPlotId(PlotArea plotArea, int x, int y, int z) {
        return new PlotId(0, 0);
    }

    @Override public Location getPlotBottomLocAbs(PlotArea plotArea, PlotId plotId) {
        return new Location(plotId.toCommaSeparatedString(), -30000000, 0, -30000000);
    }

    @Override public Location getPlotTopLocAbs(PlotArea plotArea, PlotId plotId) {
        return new Location(plotId.toCommaSeparatedString(), 30000000, 0, 30000000);
    }

    @Override public boolean clearPlot(PlotArea plotArea, Plot plot, final Runnable whenDone) {
        SetupUtils.manager.unload(plot.getWorldName(), false);
        final File worldFolder =
            new File(PlotSquared.get().IMP.getWorldContainer(), plot.getWorldName());
        TaskManager.IMP.taskAsync(() -> {
            MainUtil.deleteDirectory(worldFolder);
            if (whenDone != null)
                whenDone.run();
        });
        return true;
    }

    @Override public boolean claimPlot(PlotArea plotArea, Plot plot) {
        // TODO
        return true;
    }

    @Override public boolean unclaimPlot(PlotArea plotArea, Plot plot, Runnable whenDone) {
        if (whenDone != null)
            whenDone.run();
        return true;
    }

    @Override public Location getSignLoc(PlotArea plotArea, Plot plot) {
        return null;
    }

    @Override public String[] getPlotComponents(PlotArea plotArea, PlotId plotId) {
        return new String[0];
    }

    @Override public boolean setComponent(PlotArea plotArea, PlotId plotId, String component,
        BlockBucket blocks) {
        return false;
    }

    @Override public boolean createRoadEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouth(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouthEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean removeRoadEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouth(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouthEast(PlotArea plotArea, Plot plot) {
        return false;
    }

    @Override public boolean startPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override public boolean startPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds) {
        return false;
    }
}
