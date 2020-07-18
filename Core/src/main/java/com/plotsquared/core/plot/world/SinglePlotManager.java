/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.world;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import javax.annotation.Nonnull;

import java.io.File;
import java.util.List;

public class SinglePlotManager extends PlotManager {

    public SinglePlotManager(@Nonnull final PlotArea plotArea) {
        super(plotArea);
    }

    @Override public PlotId getPlotIdAbs(int x, int y, int z) {
        return PlotId.of(0, 0);
    }

    @Override public PlotId getPlotId(int x, int y, int z) {
        return PlotId.of(0, 0);
    }

    @Override public Location getPlotBottomLocAbs(@Nonnull final PlotId plotId) {
        return Location.at(plotId.toCommaSeparatedString(), -30000000, 0, -30000000);
    }

    @Override public Location getPlotTopLocAbs(@Nonnull final PlotId plotId) {
        return Location.at(plotId.toCommaSeparatedString(), 30000000, 0, 30000000);
    }

    @Override public boolean clearPlot(Plot plot, final Runnable whenDone) {
        PlotSquared.platform().getSetupUtils().unload(plot.getWorldName(), false);
        final File worldFolder = new File(PlotSquared.platform().getWorldContainer(), plot.getWorldName());
        TaskManager.getPlatformImplementation().taskAsync(() -> {
            MainUtil.deleteDirectory(worldFolder);
            if (whenDone != null) {
                whenDone.run();
            }
        });
        return true;
    }

    @Override public boolean claimPlot(Plot plot) {
        // TODO
        return true;
    }

    @Override public boolean unClaimPlot(Plot plot, Runnable whenDone) {
        if (whenDone != null) {
            whenDone.run();
        }
        return true;
    }

    @Override public Location getSignLoc(Plot plot) {
        return null;
    }

    @Override public String[] getPlotComponents(PlotId plotId) {
        return new String[0];
    }

    @Override public boolean setComponent(PlotId plotId, String component, Pattern blocks) {
        return false;
    }

    @Override public boolean createRoadEast(Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouth(Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadEast(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouth(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouthEast(Plot plot) {
        return false;
    }

    @Override public boolean startPlotMerge(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean startPlotUnlink(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotMerge(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotUnlink(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean regenerateAllPlotWalls() {
        return false;
    }
}
