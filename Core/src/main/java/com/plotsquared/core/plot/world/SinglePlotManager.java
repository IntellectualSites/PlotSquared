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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.world;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Override public boolean clearPlot(@Nonnull Plot plot, final Runnable whenDone, @Nullable PlotPlayer<?> actor, @Nullable QueueCoordinator queue) {
        PlotSquared.platform().getSetupUtils().unload(plot.getWorldName(), false);
        final File worldFolder = new File(PlotSquared.platform().getWorldContainer(), plot.getWorldName());
        TaskManager.getPlatformImplementation().taskAsync(() -> {
            FileUtils.deleteDirectory(worldFolder);
            if (whenDone != null) {
                whenDone.run();
            }
        });
        return true;
    }

    @Override public boolean claimPlot(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        // TODO
        return true;
    }

    @Override public boolean unClaimPlot(@Nonnull Plot plot, Runnable whenDone, @Nullable QueueCoordinator queue) {
        if (whenDone != null) {
            whenDone.run();
        }
        return true;
    }

    @Override public Location getSignLoc(@Nonnull Plot plot) {
        return null;
    }

    @Override public String[] getPlotComponents(@Nonnull PlotId plotId) {
        return new String[0];
    }

    @Override public boolean setComponent(@Nonnull PlotId plotId,
                                          @Nonnull String component,
                                          @Nonnull Pattern blocks,
                                          @Nullable PlotPlayer<?> actor,
                                          @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadSouth(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadSouthEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadSouth(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadSouthEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean startPlotMerge(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean startPlotUnlink(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean finishPlotMerge(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean finishPlotUnlink(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean regenerateAllPlotWalls(@Nullable QueueCoordinator queue) {
        return false;
    }
}
