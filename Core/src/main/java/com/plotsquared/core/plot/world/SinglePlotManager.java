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
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import org.jetbrains.annotations.NotNull;

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

    @Override public Location getPlotBottomLocAbs(@Nonnull final @NotNull PlotId plotId) {
        return Location.at(plotId.toCommaSeparatedString(), -30000000, 0, -30000000);
    }

    @Override public Location getPlotTopLocAbs(@Nonnull final @NotNull PlotId plotId) {
        return Location.at(plotId.toCommaSeparatedString(), 30000000, 0, 30000000);
    }

    @Override public boolean clearPlot(@NotNull Plot plot, final Runnable whenDone, @Nullable QueueCoordinator queue) {
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

    @Override public boolean claimPlot(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        // TODO
        return true;
    }

    @Override public boolean unClaimPlot(@NotNull Plot plot, Runnable whenDone, @Nullable QueueCoordinator queue) {
        if (whenDone != null) {
            whenDone.run();
        }
        return true;
    }

    @Override public Location getSignLoc(@NotNull Plot plot) {
        return null;
    }

    @Override public String[] getPlotComponents(@NotNull PlotId plotId) {
        return new String[0];
    }

    @Override
    public boolean setComponent(@NotNull PlotId plotId, @NotNull String component, @NotNull Pattern blocks, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadEast(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadSouth(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean createRoadSouthEast(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadEast(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadSouth(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean removeRoadSouthEast(@NotNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean startPlotMerge(@NotNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean startPlotUnlink(@NotNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean finishPlotMerge(@NotNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean finishPlotUnlink(@NotNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override public boolean regenerateAllPlotWalls(@Nullable QueueCoordinator queue) {
        return false;
    }
}
