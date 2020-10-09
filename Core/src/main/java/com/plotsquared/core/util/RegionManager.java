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
package com.plotsquared.core.util;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.BasicQueueCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Set;

public abstract class RegionManager {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + RegionManager.class.getSimpleName());

    public static RegionManager manager = null;
    private final WorldUtil worldUtil;
    private final GlobalBlockQueue blockQueue;
    private final ProgressSubscriberFactory subscriberFactory;

    @Inject public RegionManager(@Nonnull WorldUtil worldUtil, @Nonnull GlobalBlockQueue blockQueue, @Nonnull ProgressSubscriberFactory subscriberFactory) {
        this.worldUtil = worldUtil;
        this.blockQueue = blockQueue;
        this.subscriberFactory = subscriberFactory;
    }

    public static BlockVector2 getRegion(Location location) {
        int x = location.getX() >> 9;
        int z = location.getZ() >> 9;
        return BlockVector2.at(x, z);
    }

    /**
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     *
     * @param plot plot
     * @return array of counts of entity types
     */
    public abstract int[] countEntities(Plot plot);

    public void deleteRegionFiles(final String world, final Collection<BlockVector2> chunks, final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            for (BlockVector2 loc : chunks) {
                String directory = world + File.separator + "region" + File.separator + "r." + loc.getX() + "." + loc.getZ() + ".mca";
                File file = new File(PlotSquared.platform().getWorldContainer(), directory);
                logger.info("- Deleting file: {} (max 1024 chunks)", file.getName());
                if (file.exists()) {
                    file.delete();
                }
            }
            TaskManager.runTask(whenDone);
        });
    }

    /**
     * Set a number of cuboids to a certain block between two y values.
     *
     * @param area    plot area
     * @param regions cuboid regions
     * @param blocks  pattern
     * @param minY    y to set from
     * @param maxY    y to set to
     * @param actor   the actor associated with the cuboid set
     * @param queue   Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *                otherwise writes to the queue but does not enqueue.
     * @return true if not enqueued, otherwise whether the created queue enqueued.
     */
    public boolean setCuboids(@Nonnull final PlotArea area,
                              @Nonnull final Set<CuboidRegion> regions,
                              @Nonnull final Pattern blocks,
                              int minY,
                              int maxY,
                              @Nullable PlotPlayer<?> actor,
                              @Nullable QueueCoordinator queue) {
        boolean enqueue = false;
        if (queue == null) {
            queue = area.getQueue();
            enqueue = true;
            if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
                queue.addProgressSubscriber(subscriberFactory.createWithActor(actor));
            }
        }
        for (CuboidRegion region : regions) {
            Location pos1 = Location.at(area.getWorldName(), region.getMinimumPoint().getX(), minY, region.getMinimumPoint().getZ());
            Location pos2 = Location.at(area.getWorldName(), region.getMaximumPoint().getX(), maxY, region.getMaximumPoint().getZ());
            queue.setCuboid(pos1, pos2, blocks);
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Notify any plugins that may want to modify clear behaviour that a clear is occuring
     *
     * @param manager plot manager
     * @return true if the notified will accept the clear task
     */
    public boolean notifyClear(PlotManager manager) {
        return false;
    }

    /**
     * Only called when {@link RegionManager#notifyClear(PlotManager)} returns true in specific PlotManagers
     *
     * @param plot     plot
     * @param whenDone task to run when complete
     * @param manager  plot manager
     * @param actor    the player running the clear
     * @return true if the clear worked. False if someone went wrong so P2 can then handle the clear
     */
    public abstract boolean handleClear(@Nonnull Plot plot,
                                        @Nullable final Runnable whenDone,
                                        @Nonnull PlotManager manager,
                                        @Nullable PlotPlayer<?> actor);

    /**
     * Copy a region to a new location (in the same world)
     *
     * @param pos1     position 1
     * @param pos2     position 2
     * @param newPos   position to move pos1 to
     * @param actor    the actor associated with the region copy
     * @param whenDone task to run when complete
     * @return success or not
     */
    public boolean copyRegion(@Nonnull final Location pos1,
                              @Nonnull final Location pos2,
                              @Nonnull final Location newPos,
                              @Nullable final PlotPlayer<?> actor,
                              @Nonnull final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();
        final com.sk89q.worldedit.world.World oldWorld = worldUtil.getWeWorld(pos1.getWorldName());
        final com.sk89q.worldedit.world.World newWorld = worldUtil.getWeWorld(newPos.getWorldName());
        final QueueCoordinator copyFrom = blockQueue.getNewQueue(oldWorld);
        final BasicQueueCoordinator copyTo = (BasicQueueCoordinator) blockQueue.getNewQueue(newWorld);
        copyFromTo(pos1, pos2, relX, relZ, oldWorld, copyFrom, copyTo, false);
        copyFrom.setCompleteTask(copyTo::enqueue);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            copyFrom.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current copy progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        copyFrom
            .addReadChunks(new CuboidRegion(BlockVector3.at(pos1.getX(), 0, pos1.getZ()), BlockVector3.at(pos2.getX(), 0, pos2.getZ())).getChunks());
        copyTo.setCompleteTask(whenDone);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            copyTo.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current paste progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        return copyFrom.enqueue();
    }

    /**
     * Assumptions:<br>
     * - pos1 and pos2 are in the same plot<br>
     * It can be harmful to the world if parameters outside this scope are provided
     *
     * @param pos1          position 1
     * @param pos2          position 2
     * @param ignoreAugment if to bypass synchronisation ish thing
     * @param whenDone      task to run when regeneration completed
     * @return success or not
     */
    public abstract boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment, Runnable whenDone);

    public abstract void clearAllEntities(Location pos1, Location pos2);

    /**
     * Swap two regions withn the same world
     *
     * @param pos1     position 1
     * @param pos2     position 2
     * @param swapPos  position to swap with
     * @param actor    the actor associated with the region copy
     * @param whenDone task to run when complete
     */
    public void swap(Location pos1, Location pos2, Location swapPos, @Nullable final PlotPlayer<?> actor, final Runnable whenDone) {
        int relX = swapPos.getX() - pos1.getX();
        int relZ = swapPos.getZ() - pos1.getZ();

        World world1 = worldUtil.getWeWorld(pos1.getWorldName());
        World world2 = worldUtil.getWeWorld(swapPos.getWorldName());

        QueueCoordinator fromQueue1 = blockQueue.getNewQueue(world1);
        QueueCoordinator fromQueue2 = blockQueue.getNewQueue(world2);
        fromQueue1.setUnloadAfter(false);
        fromQueue2.setUnloadAfter(false);
        fromQueue1.addReadChunks(new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3()).getChunks());
        fromQueue2.addReadChunks(new CuboidRegion(swapPos.getBlockVector3(),
            BlockVector3.at(swapPos.getX() + pos2.getX() - pos1.getX(), 0, swapPos.getZ() + pos2.getZ() - pos1.getZ())).getChunks());
        QueueCoordinator toQueue1 = blockQueue.getNewQueue(world1);
        QueueCoordinator toQueue2 = blockQueue.getNewQueue(world2);

        copyFromTo(pos1, pos2, relX, relZ, world1, fromQueue1, toQueue2, true);
        copyFromTo(pos1, pos2, relX, relZ, world1, fromQueue2, toQueue1, true);
        fromQueue1.setCompleteTask(fromQueue2::enqueue);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            fromQueue1.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current region 1 copy progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        fromQueue2.setCompleteTask(toQueue1::enqueue);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            fromQueue2.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current region 2 copy progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        toQueue1.setCompleteTask(toQueue2::enqueue);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            toQueue1.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current region 1 paste progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        toQueue2.setCompleteTask(whenDone);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            toQueue2.addProgressSubscriber(subscriberFactory.createFull(actor, Settings.QUEUE.NOTIFY_INTERVAL, Settings.QUEUE.NOTIFY_WAIT,
                StaticCaption.of("<prefix><gray>Current region 2 paste progress: </gray><gold><progress></gold><gray>%</gray>")));
        }
        fromQueue1.enqueue();
    }

    private void copyFromTo(Location pos1,
                            Location pos2,
                            int relX,
                            int relZ,
                            World world1,
                            QueueCoordinator fromQueue,
                            QueueCoordinator toQueue,
                            boolean removeEntities) {
        fromQueue.setChunkConsumer(chunk -> {
            int cx = chunk.getX();
            int cz = chunk.getZ();
            int cbx = cx << 4;
            int cbz = cz << 4;
            int bx = Math.max(pos1.getX() & 15, 0);
            int bz = Math.max(pos1.getZ() & 15, 0);
            int tx = Math.min(pos2.getX() & 15, 15);
            int tz = Math.min(pos2.getZ() & 15, 15);
            for (int y = 0; y < 256; y++) {
                for (int x = bx; x <= tx; x++) {
                    for (int z = bz; z <= tz; z++) {
                        int rx = cbx + x;
                        int rz = cbz + z;
                        BlockVector3 loc = BlockVector3.at(rx, y, rz);
                        toQueue.setBlock(rx + relX, y, rz + relZ, world1.getFullBlock(loc));
                        toQueue.setBiome(rx + relX, y, rz + relZ, world1.getBiome(loc));
                    }
                }
            }
            Region region = new CuboidRegion(BlockVector3.at(cbx + bx, 0, cbz + bz), BlockVector3.at(cbx + tx, 255, cbz + tz));
            toQueue.addEntities(world1.getEntities(region));
            if (removeEntities) {
                for (Entity entity : world1.getEntities(region)) {
                    entity.remove();
                }
            }
        });
    }

    public void setBiome(final CuboidRegion region, final int extendBiome, final BiomeType biome, final String world, final Runnable whenDone) {
        Location pos1 = Location
            .at(world, region.getMinimumPoint().getX() - extendBiome, region.getMinimumPoint().getY(), region.getMinimumPoint().getZ() - extendBiome);
        Location pos2 = Location
            .at(world, region.getMaximumPoint().getX() + extendBiome, region.getMaximumPoint().getY(), region.getMaximumPoint().getZ() + extendBiome);
        final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world));

        final int minX = pos1.getX();
        final int minZ = pos1.getZ();
        final int maxX = pos2.getX();
        final int maxZ = pos2.getZ();
        queue.addReadChunks(region.getChunks());
        queue.setChunkConsumer(blockVector2 -> {
            final int cx = blockVector2.getX() << 4;
            final int cz = blockVector2.getZ() << 4;
            WorldUtil.setBiome(world, Math.max(minX, cx), Math.max(minZ, cz), Math.min(maxX, cx + 15), Math.min(maxZ, cz + 15), biome);
            worldUtil.refreshChunk(blockVector2.getBlockX(), blockVector2.getBlockZ(), world);
        });
        queue.setCompleteTask(whenDone);
        queue.enqueue();
    }
}
