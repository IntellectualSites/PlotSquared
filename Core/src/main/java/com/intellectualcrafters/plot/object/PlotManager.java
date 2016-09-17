package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.config.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public abstract class PlotManager {

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots).
     */
    public abstract PlotId getPlotIdAbs(PlotArea plotArea, int x, int y, int z);

    public abstract PlotId getPlotId(PlotArea plotArea, int x, int y, int z);

    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(PlotArea plotArea, PlotId plotId);

    // the same applies here
    public abstract Location getPlotTopLocAbs(PlotArea plotArea, PlotId plotId);

    /*
     * Plot clearing (return false if you do not support some method)
     */
    public abstract boolean clearPlot(PlotArea plotArea, Plot plot, Runnable whenDone);

    public abstract boolean claimPlot(PlotArea plotArea, Plot plot);

    public abstract boolean unclaimPlot(PlotArea plotArea, Plot plot, Runnable whenDone);

    public abstract Location getSignLoc(PlotArea plotArea, Plot plot);

    /*
     * Plot set functions (return false if you do not support the specific set
     * method).
     */
    public abstract String[] getPlotComponents(PlotArea plotArea, PlotId plotId);

    public abstract boolean setComponent(PlotArea plotArea, PlotId plotId, String component, PlotBlock[] blocks);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging).
     */
    public abstract boolean createRoadEast(PlotArea plotArea, Plot plot);

    public abstract boolean createRoadSouth(PlotArea plotArea, Plot plot);

    public abstract boolean createRoadSouthEast(PlotArea plotArea, Plot plot);

    public abstract boolean removeRoadEast(PlotArea plotArea, Plot plot);

    public abstract boolean removeRoadSouth(PlotArea plotArea, Plot plot);

    public abstract boolean removeRoadSouthEast(PlotArea plotArea, Plot plot);

    public abstract boolean startPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds);

    public abstract boolean startPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds);

    public void exportTemplate(PlotArea plotArea) throws IOException {
        HashSet<FileBytes> files = new HashSet<>(
                Collections.singletonList(new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml", Template.getBytes(plotArea))));
        Template.zipAll(plotArea.worldname, files);
    }

    public int getWorldHeight() {
        return 255;
    }

}
