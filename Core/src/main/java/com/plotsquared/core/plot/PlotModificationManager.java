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
package com.plotsquared.core.plot;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotComponentSetEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.generator.SquarePlotWorld;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.plotsquared.core.plot.Plot.MAX_HEIGHT;

/**
 * Manager that handles {@link Plot} modifications
 */
public final class PlotModificationManager {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotModificationManager.class.getSimpleName());

    private final Plot plot;

    PlotModificationManager(@Nonnull final Plot plot) {
        this.plot = plot;
    }


    /**
     * Copy a plot to a location, both physically and the settings
     *
     * @param destination destination plot
     * @return Future that completes with {@code true} if the copy was successful, else {@code false}
     */
    public CompletableFuture<Boolean> copy(@Nonnull final Plot destination) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        final PlotId offset = PlotId.of(destination.getId().getX() - this.plot.getId().getX(), destination.getId().getY() - this.plot.getId().getY());
        final Location db = destination.getBottomAbs();
        final Location ob = this.plot.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (!this.plot.hasOwner()) {
            TaskManager.runTaskLater(() -> future.complete(false), TaskTime.ticks(1L));
            return future;
        }
        final Set<Plot> plots = this.plot.getConnectedPlots();
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(destination.getArea(), offset.getX(), offset.getY());
            if (other.hasOwner()) {
                TaskManager.runTaskLater(() -> future.complete(false), TaskTime.ticks(1L));
                return future;
            }
        }
        // world border
        destination.updateWorldBorder();
        // copy data
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(destination.getArea(), offset.getX(), offset.getY());
            other.getPlotModificationManager().create(plot.getOwner(), false);
            if (!plot.getFlagContainer().getFlagMap().isEmpty()) {
                final Collection<PlotFlag<?, ?>> existingFlags = other.getFlags();
                other.getFlagContainer().clearLocal();
                other.getFlagContainer().addAll(plot.getFlagContainer().getFlagMap().values());
                // Update the database
                for (final PlotFlag<?, ?> flag : existingFlags) {
                    final PlotFlag<?, ?> newFlag = other.getFlagContainer().queryLocal(flag.getClass());
                    if (other.getFlagContainer().queryLocal(flag.getClass()) == null) {
                        DBFunc.removeFlag(other, flag);
                    } else {
                        DBFunc.setFlag(other, newFlag);
                    }
                }
            }
            if (plot.isMerged()) {
                other.setMerged(plot.getMerged());
            }
            if (plot.members != null && !plot.members.isEmpty()) {
                other.members = plot.members;
                for (UUID member : plot.members) {
                    DBFunc.setMember(other, member);
                }
            }
            if (plot.trusted != null && !plot.trusted.isEmpty()) {
                other.trusted = plot.trusted;
                for (UUID trusted : plot.trusted) {
                    DBFunc.setTrusted(other, trusted);
                }
            }
            if (plot.denied != null && !plot.denied.isEmpty()) {
                other.denied = plot.denied;
                for (UUID denied : plot.denied) {
                    DBFunc.setDenied(other, denied);
                }
            }
        }
        // copy terrain
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.plot.getRegions());
        final Runnable run = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    final QueueCoordinator queue = plot.getArea().getQueue();
                    for (final Plot current : plot.getConnectedPlots()) {
                        destination.getManager().claimPlot(current, queue);
                    }
                    if (queue.size() > 0) {
                        queue.enqueue();
                    }
                    destination.getPlotModificationManager().setSign();
                    future.complete(true);
                    return;
                }
                CuboidRegion region = regions.poll();
                Location[] corners = plot.getCorners(plot.getWorldName(), region);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location newPos = pos1.add(offsetX, 0, offsetZ).withWorld(destination.getWorldName());
                PlotSquared.platform().getRegionManager().copyRegion(pos1, pos2, newPos, this);
            }
        };
        run.run();
        return future;
    }

    /**
     * Clear the plot
     *
     * @param whenDone A runnable to execute when clearing finishes, or null
     * @see #clear(boolean, boolean, Runnable)
     * @see #deletePlot(Runnable) to clear and delete a plot
     */
    public void clear(@Nullable final Runnable whenDone) {
        this.clear(false, false, whenDone);
    }

    /**
     * Clear the plot
     *
     * @param checkRunning Whether or not already executing tasks should be checked
     * @param isDelete Whether or not the plot is being deleted
     * @param whenDone A runnable to execute when clearing finishes, or null
     * @see #deletePlot(Runnable) to clear and delete a plot
     */
    public boolean clear(final boolean checkRunning, final boolean isDelete, @Nullable final Runnable whenDone) {
        if (checkRunning && this.plot.getRunning() != 0) {
            return false;
        }
        final Set<CuboidRegion> regions = this.plot.getRegions();
        final Set<Plot> plots = this.plot.getConnectedPlots();
        final ArrayDeque<Plot> queue = new ArrayDeque<>(plots);
        if (isDelete) {
            this.removeSign();
        }
        PlotUnlinkEvent event = PlotSquared.get().getEventDispatcher()
            .callUnlink(this.plot.getArea(), this.plot, true, !isDelete, isDelete ? PlotUnlinkEvent.REASON.DELETE : PlotUnlinkEvent.REASON.CLEAR);
        if (event.getEventResult() != Result.DENY) {
            this.unlinkPlot(event.isCreateRoad(), event.isCreateSign());
        }
        final PlotManager manager = this.plot.getArea().getPlotManager();
        Runnable run = new Runnable() {
            @Override public void run() {
                if (queue.isEmpty()) {
                    Runnable run = () -> {
                        for (CuboidRegion region : regions) {
                            Location[] corners = plot.getCorners(plot.getWorldName(), region);
                            PlotSquared.platform().getRegionManager().clearAllEntities(corners[0], corners[1]);
                        }
                        TaskManager.runTask(whenDone);
                    };
                    QueueCoordinator queue = plot.getArea().getQueue();
                    for (Plot current : plots) {
                        if (isDelete || !current.hasOwner()) {
                            manager.unClaimPlot(current, null, queue);
                        } else {
                            manager.claimPlot(current, queue);
                        }
                    }
                    if (queue.size() > 0) {
                        queue.enqueue();
                    }
                    TaskManager.runTask(run);
                    return;
                }
                Plot current = queue.poll();
                if (plot.getArea().getTerrain() != PlotAreaTerrainType.NONE) {
                    try {
                        PlotSquared.platform().getRegionManager().regenerateRegion(current.getBottomAbs(), current.getTopAbs(), false, this);
                    } catch (UnsupportedOperationException exception) {
                        exception.printStackTrace();
                        return;
                    }
                    return;
                }
                manager.clearPlot(current, this, null);
            }
        };
        run.run();
        return true;
    }

    /**
     * Sets the biome for a plot asynchronously.
     *
     * @param biome    The biome e.g. "forest"
     * @param whenDone The task to run when finished, or null
     */
    public void setBiome(@Nullable final BiomeType biome, @Nonnull final Runnable whenDone) {
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.plot.getRegions());
        final int extendBiome;
        if (this.plot.getArea() instanceof SquarePlotWorld) {
            extendBiome = (((SquarePlotWorld) this.plot.getArea()).ROAD_WIDTH > 0) ? 1 : 0;
        } else {
            extendBiome = 0;
        }
        Runnable run = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = regions.poll();
                PlotSquared.platform().getRegionManager().setBiome(region, extendBiome, biome, plot.getWorldName(), this);
            }
        };
        run.run();
    }

    /**
     * Unlink the plot and all connected plots.
     *
     * @param createRoad whether to recreate road
     * @param createSign whether to recreate signs
     * @return success/!cancelled
     */
    public boolean unlinkPlot(final boolean createRoad, final boolean createSign) {
        if (!this.plot.isMerged()) {
            return false;
        }
        final Set<Plot> plots = this.plot.getConnectedPlots();
        ArrayList<PlotId> ids = new ArrayList<>(plots.size());
        for (Plot current : plots) {
            current.setHome(null);
            ids.add(current.getId());
        }
        this.plot.clearRatings();
        QueueCoordinator queue = null;
        if (createSign) {
            this.removeSign();
            queue = this.plot.getArea().getQueue();
        }
        PlotManager manager = this.plot.getArea().getPlotManager();
        if (createRoad) {
            manager.startPlotUnlink(ids, queue);
        }
        if (this.plot.getArea().getTerrain() != PlotAreaTerrainType.ALL && createRoad) {
            for (Plot current : plots) {
                if (current.isMerged(Direction.EAST)) {
                    manager.createRoadEast(current, queue);
                    if (current.isMerged(Direction.SOUTH)) {
                        manager.createRoadSouth(current, queue);
                        if (current.isMerged(Direction.SOUTHEAST)) {
                            manager.createRoadSouthEast(current, queue);
                        }
                    }
                }
                if (current.isMerged(Direction.SOUTH)) {
                    manager.createRoadSouth(current, queue);
                }
            }
        }
        for (Plot current : plots) {
            boolean[] merged = new boolean[] {false, false, false, false};
            current.setMerged(merged);
        }
        if (createSign) {
            queue.setCompleteTask(() -> TaskManager.runTaskAsync(() -> {
                for (Plot current : plots) {
                    current.getPlotModificationManager().setSign(PlayerManager.getName(current.getOwnerAbs()));
                }
            }));
        }
        if (createRoad) {
            manager.finishPlotUnlink(ids, queue);
        }
        return true;
    }

    /**
     * Sets the sign for a plot to a specific name
     *
     * @param name name
     */
    public void setSign(@Nonnull final String name) {
        if (!this.plot.isLoaded()) {
            return;
        }
        PlotManager manager = this.plot.getArea().getPlotManager();
        if (this.plot.getArea().allowSigns()) {
            Location location = manager.getSignLoc(this.plot);
            String id = this.plot.getId().toString();
            Caption[] lines =
                new Caption[] {TranslatableCaption.of("signs.owner_sign_line_1"),
                    TranslatableCaption.of("signs.owner_sign_line_2"),
                    TranslatableCaption.of("signs.owner_sign_line_3"),
                    TranslatableCaption.of("signs.owner_sign_line_4")};
            PlotSquared.platform().getWorldUtil().setSign(location, lines, Template.of("id", id), Template.of("owner", name));
        }
    }

    /**
     * Resend all chunks inside the plot to nearby players<br>
     * This should not need to be called
     */
    public void refreshChunks() {
        final HashSet<BlockVector2> chunks = new HashSet<>();
        for (final CuboidRegion region : this.plot.getRegions()) {
            for (int x = region.getMinimumPoint().getX() >> 4; x <= region.getMaximumPoint().getX() >> 4; x++) {
                for (int z = region.getMinimumPoint().getZ() >> 4; z <= region.getMaximumPoint().getZ() >> 4; z++) {
                    if (chunks.add(BlockVector2.at(x, z))) {
                        PlotSquared.platform().getWorldUtil().refreshChunk(x, z, this.plot.getWorldName());
                    }
                }
            }
        }
    }

    /**
     * Remove the plot sign if it is set.
     */
    public void removeSign() {
        PlotManager manager = this.plot.getArea().getPlotManager();
        if (!this.plot.getArea().allowSigns()) {
            return;
        }
        Location location = manager.getSignLoc(this.plot);
        QueueCoordinator queue = PlotSquared.platform().getGlobalBlockQueue()
            .getNewQueue(PlotSquared.platform().getWorldUtil().getWeWorld(this.plot.getWorldName()));
        queue.setBlock(location.getX(), location.getY(), location.getZ(), BlockTypes.AIR.getDefaultState());
        queue.enqueue();
    }

    /**
     * Sets the plot sign if plot signs are enabled.
     */
    public void setSign() {
        if (!this.plot.hasOwner()) {
            this.setSign("unknown");
            return;
        }
        PlotSquared.get().getImpromptuUUIDPipeline().getSingle(this.plot.getOwnerAbs(), (username, sign) -> this.setSign(username));
    }

    /**
     * Register a plot and create it in the database<br>
     * - The plot will not be created if the owner is null<br>
     * - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot
     * creation.
     *
     * @return true if plot was created successfully
     */
    public boolean create() {
        return this.create(this.plot.getOwnerAbs(), true);
    }

    /**
     * Register a plot and create it in the database<br>
     * - The plot will not be created if the owner is null<br>
     * - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot
     * creation.
     *
     * @param uuid   the uuid of the plot owner
     * @param notify notify
     * @return {@code true} if plot was created successfully, else {@code false}
     */
    public boolean create(@Nonnull final UUID uuid, final boolean notify) {
        this.plot.setOwnerAbs(uuid);
        Plot existing = this.plot.getArea().getOwnedPlotAbs(this.plot.getId());
        if (existing != null) {
            throw new IllegalStateException("Plot already exists!");
        }
        if (notify) {
            Integer meta = (Integer) this.plot.getArea().getMeta("worldBorder");
            if (meta != null) {
                this.plot.updateWorldBorder();
            }
        }
        Plot.connected_cache = null;
        Plot.regions_cache = null;
        this.plot.getTrusted().clear();
        this.plot.getMembers().clear();
        this.plot.getDenied().clear();
        this.plot.settings = new PlotSettings();
        if (this.plot.getArea().addPlot(this.plot)) {
            DBFunc.createPlotAndSettings(this.plot, () -> {
                PlotArea plotworld = plot.getArea();
                if (notify && plotworld.isAutoMerge()) {
                    final PlotPlayer<?> player = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);

                    PlotMergeEvent
                        event = PlotSquared.get().getEventDispatcher().callMerge(this.plot, Direction.ALL, Integer.MAX_VALUE, player);

                    if (event.getEventResult() == Result.DENY) {
                        if (player != null) {
                            player.sendMessage(TranslatableCaption.of("events.event_denied"),
                                Template.of("value", "Auto merge on claim"));
                        }
                        return;
                    }
                    plot.getPlotModificationManager().autoMerge(event.getDir(), event.getMax(), uuid, true);
                }
            });
            return true;
        }
        logger.info("Failed to add plot {} to plot area {}", this.plot.getId().toCommaSeparatedString(), this.plot.getArea().toString());
        return false;
    }

    /**
     * Remove the south road section of a plot<br>
     * - Used when a plot is merged<br>
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public void removeRoadSouth(@Nullable final QueueCoordinator queue) {
        if (this.plot.getArea().getType() != PlotAreaType.NORMAL && this.plot.getArea().getTerrain() == PlotAreaTerrainType.ROAD) {
            Plot other = this.plot.getRelative(Direction.SOUTH);
            Location bot = other.getBottomAbs();
            Location top = this.plot.getTopAbs();
            Location pos1 = Location.at(this.plot.getWorldName(), bot.getX(), 0, top.getZ());
            Location pos2 = Location.at(this.plot.getWorldName(), top.getX(), MAX_HEIGHT, bot.getZ());
            PlotSquared.platform().getRegionManager().regenerateRegion(pos1, pos2, true, null);
        } else if (this.plot.getArea().getTerrain() != PlotAreaTerrainType.ALL) { // no road generated => no road to remove
            this.plot.getManager().removeRoadSouth(this.plot, queue);
        }
    }

    /**
     * Auto merge a plot in a specific direction.
     *
     * @param dir         the direction to merge
     * @param max         the max number of merges to do
     * @param uuid        the UUID it is allowed to merge with
     * @param removeRoads whether to remove roads
     * @return {@code true} if a merge takes place, else {@code false}
     */
    public boolean autoMerge(@Nonnull final Direction dir, int max, @Nonnull final UUID uuid, final boolean removeRoads) {
        //Ignore merging if there is no owner for the plot
        if (!this.plot.hasOwner()) {
            return false;
        }
        Set<Plot> connected = this.plot.getConnectedPlots();
        HashSet<PlotId> merged = connected.stream().map(Plot::getId).collect(Collectors.toCollection(HashSet::new));
        ArrayDeque<Plot> frontier = new ArrayDeque<>(connected);
        Plot current;
        boolean toReturn = false;
        HashSet<Plot> visited = new HashSet<>();
        QueueCoordinator queue = this.plot.getArea().getQueue();
        while ((current = frontier.poll()) != null && max >= 0) {
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            Set<Plot> plots;
            if ((dir == Direction.ALL || dir == Direction.NORTH) && !this.plot.isMerged(Direction.NORTH)) {
                Plot other = current.getRelative(Direction.NORTH);
                if (other != null && other.isOwner(uuid) && (other.getBasePlot(false).equals(current.getBasePlot(false))
                    || (plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads, queue);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.plot.getManager().finishPlotMerge(ids, queue);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.EAST) && !current.isMerged(Direction.EAST)) {
                Plot other = current.getRelative(Direction.EAST);
                if (other != null && other.isOwner(uuid) && (other.getBasePlot(false).equals(current.getBasePlot(false))
                    || (plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads, queue);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.plot.getManager().finishPlotMerge(ids, queue);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.SOUTH) && !this.plot.isMerged(Direction.SOUTH)) {
                Plot other = current.getRelative(Direction.SOUTH);
                if (other != null && other.isOwner(uuid) && (other.getBasePlot(false).equals(current.getBasePlot(false))
                    || (plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads, queue);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.plot.getManager().finishPlotMerge(ids, queue);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.WEST) && !this.plot.isMerged(Direction.WEST)) {
                Plot other = current.getRelative(Direction.WEST);
                if (other != null && other.isOwner(uuid) && (other.getBasePlot(false).equals(current.getBasePlot(false))
                    || (plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads, queue);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.plot.getManager().finishPlotMerge(ids, queue);
                    }
                }
            }
            if (queue.size() > 0) {
                queue.enqueue();
            }
        }
        return toReturn;
    }

    /**
     * Moves a plot physically, as well as the corresponding settings.
     *
     * @param destination Plot moved to
     * @param whenDone    task when done
     * @param allowSwap   whether to swap plots
     * @return {@code true} if the move was successful, else {@code false}
     */
    @Nonnull public CompletableFuture<Boolean> move(@Nonnull final Plot destination,
                                                    @Nonnull final Runnable whenDone,
                                                    final boolean allowSwap) {
        final PlotId offset = PlotId.of(destination.getId().getX() - this.plot.getId().getX(), destination.getId().getY() - this.plot.getId().getY());
        Location db = destination.getBottomAbs();
        Location ob = this.plot.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (!this.plot.hasOwner()) {
            TaskManager.runTaskLater(whenDone, TaskTime.ticks(1L));
            return CompletableFuture.completedFuture(false);
        }
        AtomicBoolean occupied = new AtomicBoolean(false);
        Set<Plot> plots = this.plot.getConnectedPlots();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(destination.getArea(), offset.getX(), offset.getY());
            if (other.hasOwner()) {
                if (!allowSwap) {
                    TaskManager.runTaskLater(whenDone, TaskTime.ticks(1L));
                    return CompletableFuture.completedFuture(false);
                }
                occupied.set(true);
            } else {
                plot.getPlotModificationManager().removeSign();
            }
        }
        // world border
        destination.updateWorldBorder();
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.plot.getRegions());
        // move / swap data
        final PlotArea originArea = this.plot.getArea();

        final Iterator<Plot> plotIterator = plots.iterator();

        CompletableFuture<Boolean> future = null;
        if (plotIterator.hasNext()) {
            while (plotIterator.hasNext()) {
                final Plot plot = plotIterator.next();
                final Plot other = plot.getRelative(destination.getArea(), offset.getX(), offset.getY());
                final CompletableFuture<Boolean> swapResult = plot.swapData(other);
                if (future == null) {
                    future = swapResult;
                } else {
                    future = future.thenCombine(swapResult, (fn, th) -> fn);
                }
            }
        } else {
            future = CompletableFuture.completedFuture(true);
        }

        return future.thenApply(result -> {
            if (!result) {
                return false;
            }
            // copy terrain
            if (occupied.get()) {
                new Runnable() {
                    @Override public void run() {
                        if (regions.isEmpty()) {
                            // Update signs
                            destination.getPlotModificationManager().setSign();
                            setSign();
                            // Run final tasks
                            TaskManager.runTask(whenDone);
                        } else {
                            CuboidRegion region = regions.poll();
                            Location[] corners = plot.getCorners(plot.getWorldName(), region);
                            Location pos1 = corners[0];
                            Location pos2 = corners[1];
                            Location pos3 = pos1.add(offsetX, 0, offsetZ).withWorld(destination.getWorldName());
                            PlotSquared.platform().getRegionManager().swap(pos1, pos2, pos3, this);
                        }
                    }
                }.run();
            } else {
                new Runnable() {
                    @Override public void run() {
                        if (regions.isEmpty()) {
                            Plot plot = destination.getRelative(0, 0);
                            Plot originPlot = originArea.getPlotAbs(PlotId.of(plot.getId().getX() - offset.getX(), plot.getId().getY() - offset.getY()));
                            final Runnable clearDone = () -> {
                                QueueCoordinator queue = PlotModificationManager.this.plot.getArea().getQueue();
                                for (final Plot current : plot.getConnectedPlots()) {
                                    PlotModificationManager.this.plot.getManager().claimPlot(current, queue);
                                }
                                if (queue.size() > 0) {
                                    queue.enqueue();
                                }
                                plot.getPlotModificationManager().setSign();
                                TaskManager.runTask(whenDone);
                            };
                            if (originPlot != null) {
                                originPlot.getPlotModificationManager().clear(false, true, clearDone);
                            } else {
                                clearDone.run();
                            }
                            return;
                        }
                        final Runnable task = this;
                        CuboidRegion region = regions.poll();
                        Location[] corners = PlotModificationManager.this.plot.getCorners(PlotModificationManager.this.plot.getWorldName(), region);
                        final Location pos1 = corners[0];
                        final Location pos2 = corners[1];
                        Location newPos = pos1.add(offsetX, 0, offsetZ).withWorld(destination.getWorldName());
                        PlotSquared.platform().getRegionManager().copyRegion(pos1, pos2, newPos, task);
                    }
                }.run();
            }
            return true;
        });
    }

    /**
     * Unlink a plot and remove the roads
     *
     * @return true if plot was linked
     * @see #unlinkPlot(boolean, boolean)
     */
    public boolean unlink() {
        return this.unlinkPlot(true, true);
    }

    /**
     * Swap the plot contents and settings with another location<br>
     * - The destination must correspond to a valid plot of equal dimensions
     *
     * @param destination The other plot to swap with
     * @param whenDone    A task to run when finished, or null
     * @return Future that completes with {@code true} if the swap was successful, else {@code false}
     */
    @Nonnull public CompletableFuture<Boolean> swap(@Nonnull final Plot destination, @Nonnull final Runnable whenDone) {
        return this.move(destination, whenDone, true);
    }

    /**
     * Moves the plot to an empty location<br>
     * - The location must be empty
     *
     * @param destination Where to move the plot
     * @param whenDone    A task to run when done, or null
     * @return Future that completes with {@code true} if the move was successful, else {@code false}
     */
    @Nonnull public CompletableFuture<Boolean> move(@Nonnull final Plot destination, @Nonnull final Runnable whenDone) {
        return this.move(destination, whenDone, false);
    }

    /**
     * Sets a component for a plot to the provided blocks<br>
     * - E.g. floor, wall, border etc.<br>
     * - The available components depend on the generator being used<br>
     *
     * @param component Component to set
     * @param blocks    Pattern to use the generation
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     * @return {@code true} if the component was set successfully, else {@code false}
     */
    public boolean setComponent(@Nonnull final String component, @Nonnull final Pattern blocks, @Nullable final QueueCoordinator queue) {
        final PlotComponentSetEvent event = PlotSquared.get().getEventDispatcher().callComponentSet(this.plot, component, blocks);
        return this.plot.getManager().setComponent(this.plot.getId(), event.getComponent(), event.getPattern(), queue);
    }

    /**
     * Delete a plot (use null for the runnable if you don't need to be notified on completion)
     *
     * @see PlotSquared#removePlot(Plot, boolean)
     * @see PlotModificationManager#clear(boolean, boolean, Runnable) to simply clear a plot
     *
     * @param whenDone task to run when plot has been deleted. Nullable
     *
     * @return {@code true} if the deletion was successful, {@code false} if not
     */
    public boolean deletePlot(final Runnable whenDone) {
        if (!this.plot.hasOwner()) {
            return false;
        }
        final Set<Plot> plots = this.plot.getConnectedPlots();
        this.clear(false, true, () -> {
            for (Plot current : plots) {
                current.unclaim();
            }
            TaskManager.runTask(whenDone);
        });
        return true;
    }

    /**
     * Sets components such as border, wall, floor.
     * (components are generator specific)
     *
     * @param component component to set
     * @param blocks string of block(s) to set component to
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     *
     * @return {@code true} if the update was successful, {@code false} if not
     */
    @Deprecated public boolean setComponent(String component, String blocks, QueueCoordinator queue) {
        final BlockBucket parsed = ConfigurationUtil.BLOCK_BUCKET.parseString(blocks);
        if (parsed != null && parsed.isEmpty()) {
            return false;
        }
        return this.setComponent(component, parsed.toPattern(), queue);
    }

    /**
     * Remove the east road section of a plot<br>
     * - Used when a plot is merged<br>
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public void removeRoadEast(@Nullable QueueCoordinator queue) {
        if (this.plot.getArea().getType() != PlotAreaType.NORMAL && this.plot.getArea().getTerrain() == PlotAreaTerrainType.ROAD) {
            Plot other = this.plot.getRelative(Direction.EAST);
            Location bot = other.getBottomAbs();
            Location top = this.plot.getTopAbs();
            Location pos1 = Location.at(this.plot.getWorldName(), top.getX(), 0, bot.getZ());
            Location pos2 = Location.at(this.plot.getWorldName(), bot.getX(), MAX_HEIGHT, top.getZ());
            PlotSquared.platform().getRegionManager().regenerateRegion(pos1, pos2, true, null);
        } else if (this.plot.getArea().getTerrain() != PlotAreaTerrainType.ALL) { // no road generated => no road to remove
            this.plot.getArea().getPlotManager().removeRoadEast(this.plot, queue);
        }
    }

    /**
     * Remove the SE road (only effects terrain)
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public void removeRoadSouthEast(@Nullable QueueCoordinator queue) {
        if (this.plot.getArea().getType() != PlotAreaType.NORMAL && this.plot.getArea().getTerrain() == PlotAreaTerrainType.ROAD) {
            Plot other = this.plot.getRelative(1, 1);
            Location pos1 = this.plot.getTopAbs().add(1, 0, 1).withY(0);
            Location pos2 = other.getBottomAbs().subtract(1, 0, 1).withY(MAX_HEIGHT);
            PlotSquared.platform().getRegionManager().regenerateRegion(pos1, pos2, true, null);
        } else if (this.plot.getArea().getTerrain() != PlotAreaTerrainType.ALL) { // no road generated => no road to remove
            this.plot.getArea().getPlotManager().removeRoadSouthEast(this.plot, queue);
        }
    }

}
