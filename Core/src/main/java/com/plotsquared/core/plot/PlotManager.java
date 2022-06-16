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
package com.plotsquared.core.plot;

import com.plotsquared.core.command.Template;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.FileBytes;
import com.sk89q.worldedit.function.pattern.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class PlotManager {

    private final PlotArea plotArea;

    public PlotManager(@NonNull PlotArea plotArea) {
        this.plotArea = plotArea;
    }

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots).
     */
    public abstract PlotId getPlotIdAbs(int x, int y, int z);

    public abstract PlotId getPlotId(int x, int y, int z);

    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(@NonNull PlotId plotId);

    // the same applies here
    public abstract Location getPlotTopLocAbs(@NonNull PlotId plotId);

    public abstract boolean clearPlot(
            @NonNull Plot plot,
            @Nullable Runnable whenDone,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    );

    public abstract boolean claimPlot(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Completes block changes associated with plot unclaim.
     *
     * @param plot     plot to unclaim
     * @param whenDone task to run when plot is unclaimed
     * @param queue    Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *                 otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean unClaimPlot(@NonNull Plot plot, @Nullable Runnable whenDone, @Nullable QueueCoordinator queue);

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    public abstract Location getSignLoc(@NonNull Plot plot);

    /**
     * Get an array of the plot's component values as string
     *
     * @param plotId plotId to get components of
     * @return array of plot's component values
     */
    public abstract String[] getPlotComponents(@NonNull PlotId plotId);

    /**
     * Set the specified components to the specified Pattern on the specified plot.
     *
     * @param plotId    id of plot to set component to
     * @param component FLOOR, WALL, AIR, MAIN, MIDDLE, OUTLINE, BORDER, ALL (floor, air and main).
     * @param blocks    Pattern to set component to
     * @param actor     The player executing the task
     * @param queue     Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *                  otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean setComponent(
            @NonNull PlotId plotId,
            @NonNull String component,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    );

    /**
     * Create the road east of the plot (not schematic-based)
     *
     * @param plot  plot to create the road for
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean createRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Create the road south of the plot (not schematic-based)
     *
     * @param plot  plot to create the road for
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean createRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Create the south-east corner of the road (intersection, not schematic-based)
     *
     * @param plot  plot to create the road for
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean createRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Replace the road to the east of the plot with standard plot blocks (for when merging plots)
     *
     * @param plot  plot to remove east road from
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean removeRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Replace the road to the south of the plot with standard plot blocks (for when merging plots)
     *
     * @param plot  plot to remove south road from
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean removeRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    /**
     * Replace the road to the south east of the plot (intersection) with standard plot blocks (for when merging plots)
     *
     * @param plot  plot to remove south east road intersection from
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean removeRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean startPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    public abstract boolean startPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED).
     *
     * @param plotIds list of PlotIds to finish the merge for
     * @param queue   Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *                otherwise writes to the queue but does not enqueue.
     * @return {@code false} if part if the merge failed, otherwise {@code true} if successful.
     */
    public abstract boolean finishPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    /**
     * Finished off an unlink by resetting the top wall block for unlinked plots
     *
     * @param plotIds list of PlotIds to reset the top wall block of
     * @param queue   Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *                otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public abstract boolean finishPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    public void exportTemplate() throws IOException {
        HashSet<FileBytes> files =
                new HashSet<>(Collections.singletonList(new FileBytes(
                        Settings.Paths.TEMPLATES + "/tmp-data.yml",
                        Template.getBytes(plotArea)
                )));
        Template.zipAll(plotArea.getWorldName(), files);
    }

    /**
     * Sets all the blocks along all the plot walls to their correct state (claimed or unclaimed).
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return {@code true} if the wall blocks were successfully set
     */
    public boolean regenerateAllPlotWalls(@Nullable QueueCoordinator queue) {
        boolean success = true;
        for (Plot plot : plotArea.getPlots()) {
            if (plot.hasOwner()) {
                success &= claimPlot(plot, queue);
            } else {
                success &= unClaimPlot(plot, null, queue);
            }
        }
        return success;
    }

}
