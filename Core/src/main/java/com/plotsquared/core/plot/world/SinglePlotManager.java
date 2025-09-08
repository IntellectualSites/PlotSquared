/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.List;

public class SinglePlotManager extends PlotManager {

    private static final int MAX_COORDINATE = 20000000;

    public SinglePlotManager(final @NonNull PlotArea plotArea) {
        super(plotArea);
    }

    @Override
    public PlotId getPlotIdAbs(int x, int y, int z) {
        return PlotId.of(0, 0);
    }

    @Override
    public PlotId getPlotId(int x, int y, int z) {
        return PlotId.of(0, 0);
    }

    @Override
    public Location getPlotBottomLocAbs(final @NonNull PlotId plotId) {
        return Location.at(plotId.toUnderscoreSeparatedString(), -MAX_COORDINATE, 0, -MAX_COORDINATE);
    }

    @Override
    public Location getPlotTopLocAbs(final @NonNull PlotId plotId) {
        return Location.at(plotId.toUnderscoreSeparatedString(), MAX_COORDINATE, 0, MAX_COORDINATE);
    }

    @Override
    public boolean clearPlot(
            @NonNull Plot plot,
            final Runnable whenDone,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        PlotSquared.platform().setupUtils().unload(plot.getWorldName(), false);
        final File worldFolder = new File(PlotSquared.platform().worldContainer(), plot.getWorldName());
        TaskManager.getPlatformImplementation().taskAsync(() -> {
            FileUtils.deleteDirectory(worldFolder);
            if (whenDone != null) {
                whenDone.run();
            }
        });
        return true;
    }

    @Override
    public boolean claimPlot(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        // TODO
        return true;
    }

    @Override
    public boolean unClaimPlot(@NonNull Plot plot, Runnable whenDone, @Nullable QueueCoordinator queue) {
        if (whenDone != null) {
            whenDone.run();
        }
        return true;
    }

    @Override
    public Location getSignLoc(@NonNull Plot plot) {
        return null;
    }

    @Override
    public String[] getPlotComponents(@NonNull PlotId plotId) {
        return new String[0];
    }

    @Override
    public boolean setComponent(
            @NonNull PlotId plotId,
            @NonNull String component,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        return false;
    }

    @Override
    public boolean createRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean createRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean createRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean removeRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean removeRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean removeRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean startPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean startPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean finishPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean finishPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return false;
    }

    @Override
    public boolean regenerateAllPlotWalls(@Nullable QueueCoordinator queue) {
        return false;
    }

}
