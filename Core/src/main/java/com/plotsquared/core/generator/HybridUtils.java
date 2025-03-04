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
package com.plotsquared.core.generator;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.listener.WEExtent;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.expiration.PlotAnalysis;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnalysisFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.queue.BlockArrayCacheScopedQueueCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HybridUtils {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + HybridUtils.class.getSimpleName());
    private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    /**
     * Deprecated and likely to be removed in a future release.
     */
    @Deprecated(forRemoval = true, since = "7.0.0")
    public static HybridUtils manager;
    public static Set<BlockVector2> regions;
    public static int height;
    // Use ordered for reasonable chunk loading order to reduce paper unloading neighbour chunks and then us attempting to load
    // them again, causing errors
    public static Set<BlockVector2> chunks = new LinkedHashSet<>();
    public static PlotArea area;
    public static boolean UPDATE = false;

    private final PlotAreaManager plotAreaManager;
    private final ChunkManager chunkManager;
    private final GlobalBlockQueue blockQueue;
    private final WorldUtil worldUtil;
    private final SchematicHandler schematicHandler;
    private final EventDispatcher eventDispatcher;

    @Inject
    public HybridUtils(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull ChunkManager chunkManager,
            final @NonNull GlobalBlockQueue blockQueue,
            final @NonNull WorldUtil worldUtil,
            final @NonNull SchematicHandler schematicHandler,
            final @NonNull EventDispatcher eventDispatcher
    ) {
        this.plotAreaManager = plotAreaManager;
        this.chunkManager = chunkManager;
        this.blockQueue = blockQueue;
        this.worldUtil = worldUtil;
        this.schematicHandler = schematicHandler;
        this.eventDispatcher = eventDispatcher;
    }

    public void regeneratePlotWalls(final PlotArea area) {
        PlotManager plotManager = area.getPlotManager();
        plotManager.regenerateAllPlotWalls(null);
    }

    public void analyzeRegion(final String world, final CuboidRegion region, final RunnableVal<PlotAnalysis> whenDone) {
        // int diff, int variety, int vertices, int rotation, int height_sd
        /*
         * diff: compare to base by looping through all blocks
         * variety: add to HashSet for each BlockState
         * height_sd: loop over all blocks and get top block
         *
         * vertices: store air map and compare with neighbours
         * for each block check the adjacent
         *  - Store all blocks then go through in second loop
         *  - recheck each block
         *
         */
        TaskManager.runTaskAsync(() -> {
            final PlotArea area = this.plotAreaManager.getPlotArea(world, null);
            if (!(area instanceof HybridPlotWorld hpw)) {
                return;
            }

            final BlockVector3 bot = region.getMinimumPoint();
            final BlockVector3 top = region.getMaximumPoint();

            final int bx = bot.getX();
            final int bz = bot.getZ();
            final int tx = top.getX();
            final int tz = top.getZ();
            final int cbx = bx >> 4;
            final int cbz = bz >> 4;
            final int ctx = tx >> 4;
            final int ctz = tz >> 4;
            final int width = tx - bx + 1;
            final int length = tz - bz + 1;
            final int height = area.getMaxGenHeight() - area.getMinGenHeight() + 1;
            final int minHeight = area.getMinGenHeight();

            final BlockState[][][] newBlocks = new BlockState[height][width][length];

            BlockArrayCacheScopedQueueCoordinator oldBlockQueue = new BlockArrayCacheScopedQueueCoordinator(
                    Location.at("", region.getMinimumPoint().withY(hpw.getMinGenHeight())),
                    Location.at("", region.getMaximumPoint().withY(hpw.getMaxGenHeight()))
            );

            region.getChunks().forEach(chunkPos -> {
                int relChunkX = chunkPos.getX() - cbx;
                int relChunkZ = chunkPos.getZ() - cbz;
                oldBlockQueue.setOffsetX(relChunkX << 4);
                oldBlockQueue.setOffsetZ(relChunkZ << 4);
                hpw.getGenerator().generateChunk(oldBlockQueue, hpw, false);
            });

            final BlockState[][][] oldBlocks = oldBlockQueue.getBlockStates();

            QueueCoordinator queue = area.getQueue();
            queue.addReadChunks(region.getChunks());
            queue.setChunkConsumer(chunkPos -> {
                int X = chunkPos.getX();
                int Z = chunkPos.getZ();
                int minX;
                if (X == cbx) {
                    minX = bx & 15;
                } else {
                    minX = 0;
                }
                int minZ;
                if (Z == cbz) {
                    minZ = bz & 15;
                } else {
                    minZ = 0;
                }
                int maxX;
                if (X == ctx) {
                    maxX = tx & 15;
                } else {
                    maxX = 15;
                }
                int maxZ;
                if (Z == ctz) {
                    maxZ = tz & 15;
                } else {
                    maxZ = 15;
                }

                int chunkBlockX = X << 4;
                int chunkBlockZ = Z << 4;

                int xb = chunkBlockX - bx;
                int zb = chunkBlockZ - bz;
                for (int x = minX; x <= maxX; x++) {
                    int xx = chunkBlockX + x;
                    for (int z = minZ; z <= maxZ; z++) {
                        int zz = chunkBlockZ + z;
                        for (int yIndex = 0; yIndex < height; yIndex++) {
                            int y = yIndex + minHeight;
                            BlockState block = queue.getBlock(xx, y, zz);
                            if (block == null) {
                                block = AIR;
                            }
                            int xr = xb + x;
                            int zr = zb + z;
                            newBlocks[yIndex][xr][zr] = block;
                        }
                    }
                }
            });

            final Runnable run = () -> {
                int size = width * length;
                int[] changes = new int[size];
                int[] faces = new int[size];
                int[] data = new int[size];
                int[] air = new int[size];
                int[] variety = new int[size];
                int i = 0;
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        Set<BlockType> types = new HashSet<>();
                        for (int yIndex = 0; yIndex < height; yIndex++) {
                            BlockState old = oldBlocks[yIndex][x][z]; // Nullable
                            BlockState now = newBlocks[yIndex][x][z]; // Not null
                            if (now == null) {
                                throw new NullPointerException(String.format(
                                        "\"now\" block null attempting to perform plot analysis. Indexes: x=%d of %d, yIndex=%d" +
                                                " of %d, z=%d of %d",
                                        x,
                                        width,
                                        yIndex,
                                        height,
                                        z,
                                        length
                                ));
                            }
                            if (!now.equals(old) && !(old == null && now.getBlockType().equals(BlockTypes.AIR))) {
                                changes[i]++;
                            }
                            if (now.getBlockType().getMaterial().isAir()) {
                                air[i]++;
                            } else {
                                // check vertices
                                // modifications_adjacent
                                if (x > 0 && z > 0 && yIndex > 0 && x < width - 1 && z < length - 1 && yIndex < (height - 1)) {
                                    if (newBlocks[yIndex - 1][x][z].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                    if (newBlocks[yIndex][x - 1][z].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                    if (newBlocks[yIndex][x][z - 1].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                    if (newBlocks[yIndex + 1][x][z].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                    if (newBlocks[yIndex][x + 1][z].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                    if (newBlocks[yIndex][x][z + 1].getBlockType().getMaterial().isAir()) {
                                        faces[i]++;
                                    }
                                }

                                if (!now.equals(now.getBlockType().getDefaultState())) {
                                    data[i]++;
                                }
                                types.add(now.getBlockType());
                            }
                        }
                        variety[i] = types.size();
                        i++;
                    }
                }
                // analyze plot
                // put in analysis obj

                // run whenDone
                PlotAnalysis analysis = new PlotAnalysis();
                analysis.changes = (int) (MathMan.getMean(changes) * 100);
                analysis.faces = (int) (MathMan.getMean(faces) * 100);
                analysis.data = (int) (MathMan.getMean(data) * 100);
                analysis.air = (int) (MathMan.getMean(air) * 100);
                analysis.variety = (int) (MathMan.getMean(variety) * 100);

                analysis.changes_sd = (int) (MathMan.getSD(changes, analysis.changes) * 100);
                analysis.faces_sd = (int) (MathMan.getSD(faces, analysis.faces) * 100);
                analysis.data_sd = (int) (MathMan.getSD(data, analysis.data) * 100);
                analysis.air_sd = (int) (MathMan.getSD(air, analysis.air) * 100);
                analysis.variety_sd = (int) (MathMan.getSD(variety, analysis.variety) * 100);
                whenDone.value = analysis;
                whenDone.run();
            };
            queue.setCompleteTask(run);
            queue.enqueue();
        });
    }

    public void analyzePlot(final Plot origin, final RunnableVal<PlotAnalysis> whenDone) {
        final ArrayDeque<CuboidRegion> zones = new ArrayDeque<>(origin.getRegions());
        final ArrayList<PlotAnalysis> analysis = new ArrayList<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (zones.isEmpty()) {
                    if (!analysis.isEmpty()) {
                        whenDone.value = new PlotAnalysis();
                        for (PlotAnalysis data : analysis) {
                            whenDone.value.air += data.air;
                            whenDone.value.air_sd += data.air_sd;
                            whenDone.value.changes += data.changes;
                            whenDone.value.changes_sd += data.changes_sd;
                            whenDone.value.data += data.data;
                            whenDone.value.data_sd += data.data_sd;
                            whenDone.value.faces += data.faces;
                            whenDone.value.faces_sd += data.faces_sd;
                            whenDone.value.variety += data.variety;
                            whenDone.value.variety_sd += data.variety_sd;
                        }
                        whenDone.value.air /= analysis.size();
                        whenDone.value.air_sd /= analysis.size();
                        whenDone.value.changes /= analysis.size();
                        whenDone.value.changes_sd /= analysis.size();
                        whenDone.value.data /= analysis.size();
                        whenDone.value.data_sd /= analysis.size();
                        whenDone.value.faces /= analysis.size();
                        whenDone.value.faces_sd /= analysis.size();
                        whenDone.value.variety /= analysis.size();
                        whenDone.value.variety_sd /= analysis.size();
                    } else {
                        whenDone.value = analysis.get(0);
                    }
                    List<Integer> result = new ArrayList<>();
                    result.add(whenDone.value.changes);
                    result.add(whenDone.value.faces);
                    result.add(whenDone.value.data);
                    result.add(whenDone.value.air);
                    result.add(whenDone.value.variety);

                    result.add(whenDone.value.changes_sd);
                    result.add(whenDone.value.faces_sd);
                    result.add(whenDone.value.data_sd);
                    result.add(whenDone.value.air_sd);
                    result.add(whenDone.value.variety_sd);
                    PlotFlag<?, ?> plotFlag = GlobalFlagContainer.getInstance().getFlag(AnalysisFlag.class).createFlagInstance(
                            result);
                    PlotFlagAddEvent event = eventDispatcher.callFlagAdd(plotFlag, origin);
                    if (event.getEventResult() == Result.DENY) {
                        return;
                    }
                    origin.setFlag(event.getFlag());
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = zones.poll();
                final Runnable task = this;
                analyzeRegion(origin.getWorldName(), region, new RunnableVal<>() {
                    @Override
                    public void run(PlotAnalysis value) {
                        analysis.add(value);
                        TaskManager.runTaskLater(task, TaskTime.ticks(1L));
                    }
                });
            }
        };
        run.run();
    }

    public final ArrayList<BlockVector2> getChunks(BlockVector2 region) {
        ArrayList<BlockVector2> chunks = new ArrayList<>();
        int sx = region.getX() << 5;
        int sz = region.getZ() << 5;
        for (int x = sx; x < sx + 32; x++) {
            for (int z = sz; z < sz + 32; z++) {
                chunks.add(BlockVector2.at(x, z));
            }
        }
        return chunks;
    }

    public boolean scheduleRoadUpdate(PlotArea area, int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        Set<BlockVector2> regions = this.worldUtil.getChunkChunks(area.getWorldName());
        return scheduleRoadUpdate(area, regions, extend, new LinkedHashSet<>());
    }

    public boolean scheduleSingleRegionRoadUpdate(Plot plot, int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        Set<BlockVector2> regions = new HashSet<>();
        regions.add(RegionManager.getRegion(plot.getCenterSynchronous()));
        return scheduleRoadUpdate(plot.getArea(), regions, extend, new LinkedHashSet<>());
    }

    public boolean scheduleRoadUpdate(
            final PlotArea area,
            Set<BlockVector2> regions,
            final int extend,
            Set<BlockVector2> chunks
    ) {
        HybridUtils.regions = regions;
        HybridUtils.area = area;
        HybridUtils.height = extend;
        HybridUtils.chunks = chunks;
        final int initial = 1024 * regions.size() + chunks.size();
        final AtomicInteger count = new AtomicInteger(0);
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                if (!UPDATE) {
                    Iterator<BlockVector2> iter = chunks.iterator();
                    QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(area.getWorldName()));
                    queue.setShouldGen(false);
                    while (iter.hasNext()) {
                        BlockVector2 chunk = iter.next();
                        iter.remove();
                        boolean regenedRoad = regenerateRoad(area, chunk, extend, queue);
                        if (!regenedRoad) {
                            LOGGER.info("Failed to regenerate roads in chunk {}", chunk);
                        }
                    }
                    queue.enqueue();
                    LOGGER.info("Cancelled road task");
                    return;
                }
                count.incrementAndGet();
                if (count.intValue() % 10 == 0) {
                    LOGGER.info("Progress: {}%", 100 * (initial - (chunks.size() + 1024 * regions.size())) / initial);
                }
                if (HybridUtils.regions.isEmpty() && chunks.isEmpty()) {
                    regeneratePlotWalls(area);

                    HybridUtils.UPDATE = false;
                    LOGGER.info("Finished road conversion");
                    // CANCEL TASK
                } else {
                    final Runnable task = this;
                    TaskManager.runTaskAsync(() -> {
                        try {
                            if (chunks.size() < 64) {
                                if (!HybridUtils.regions.isEmpty()) {
                                    Iterator<BlockVector2> iterator = HybridUtils.regions.iterator();
                                    BlockVector2 loc = iterator.next();
                                    iterator.remove();
                                    LOGGER.info("Updating .mcr: {}, {} (approx 1024 chunks)", loc.getX(), loc.getZ());
                                    LOGGER.info("- Remaining: {}", HybridUtils.regions.size());
                                    chunks.addAll(getChunks(loc));
                                    System.gc();
                                }
                            }
                            if (!chunks.isEmpty()) {
                                TaskManager.getPlatformImplementation().sync(() -> {
                                    Iterator<BlockVector2> iterator = chunks.iterator();
                                    if (chunks.size() >= 32) {
                                        QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(area.getWorldName()));
                                        queue.setShouldGen(false);
                                        for (int i = 0; i < 32; i++) {
                                            final BlockVector2 chunk = iterator.next();
                                            iterator.remove();
                                            boolean regenedRoads = regenerateRoad(area, chunk, extend, queue);
                                            if (!regenedRoads) {
                                                LOGGER.info("Failed to regenerate the road in chunk {}", chunk);
                                            }
                                        }
                                        queue.setCompleteTask(task);
                                        queue.enqueue();
                                        return null;
                                    }
                                    QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(area.getWorldName()));
                                    queue.setShouldGen(false);
                                    while (!chunks.isEmpty()) {
                                        final BlockVector2 chunk = iterator.next();
                                        iterator.remove();
                                        boolean regenedRoads = regenerateRoad(area, chunk, extend, queue);
                                        if (!regenedRoads) {
                                            LOGGER.info("Failed to regenerate road in chunk {}", chunk);
                                        }
                                    }
                                    queue.setCompleteTask(task);
                                    queue.enqueue();
                                    return null;
                                });
                                return;
                            }
                        } catch (Exception e) {
                            Iterator<BlockVector2> iterator = HybridUtils.regions.iterator();
                            BlockVector2 loc = iterator.next();
                            iterator.remove();
                            LOGGER.error(
                                    "Error! Could not update '{}/region/r.{}.{}.mca' (Corrupt chunk?)",
                                    area.getWorldHash(),
                                    loc.getX(),
                                    loc.getZ(),
                                    e
                            );
                        }
                        TaskManager.runTaskLater(task, TaskTime.seconds(1L));
                    });
                }
            }
        });
        return true;
    }

    public boolean setupRoadSchematic(Plot plot) {
        final String world = plot.getWorldName();
        final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world));
        Location bot = plot.getBottomAbs().subtract(1, 0, 1);
        Location top = plot.getTopAbs();
        final HybridPlotWorld plotworld = (HybridPlotWorld) plot.getArea();
        // Do not use plotworld#schematicStartHeight() here as we want to restore the pre 6.1.4 way of doing it if
        //  USE_WALL_IN_ROAD_SCHEM_HEIGHT is false
        int schemY = Settings.Schematics.USE_WALL_IN_ROAD_SCHEM_HEIGHT ?
                Math.min(plotworld.PLOT_HEIGHT, Math.min(plotworld.WALL_HEIGHT, plotworld.ROAD_HEIGHT)) : plotworld.ROAD_HEIGHT;
        int sx = bot.getX() - plotworld.ROAD_WIDTH + 1;
        int sz = bot.getZ() + 1;
        int sy = Settings.Schematics.PASTE_ROAD_ON_TOP ? schemY : plot.getArea().getMinGenHeight();
        int ex = bot.getX();
        int ez = top.getZ();
        int ey = get_ey(plotworld, queue, sx, ex, sz, ez, sy);
        int bz = sz - plotworld.ROAD_WIDTH;
        int tz = sz - 1;
        int ty = get_ey(plotworld, queue, sx, ex, bz, tz, sy);

        final Set<CuboidRegion> sideRoad = Collections.singleton(RegionUtil.createRegion(sx, ex, sy, ey, sz, ez));
        final Set<CuboidRegion> intersection = Collections.singleton(RegionUtil.createRegion(sx, ex, sy, ty, bz, tz));

        final String dir = Settings.Paths.SCHEMATICS + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot
                .getArea()
                .toString() + File.separator;

        this.schematicHandler.getCompoundTag(world, sideRoad)
                .whenComplete((compoundTag, throwable) -> {
                    schematicHandler.save(compoundTag, dir + "sideroad.schem");
                    schematicHandler.getCompoundTag(world, intersection)
                            .whenComplete((c, t) -> {
                                schematicHandler.save(c, dir + "intersection.schem");
                                plotworld.ROAD_SCHEMATIC_ENABLED = true;
                                try {
                                    plotworld.setupSchematics();
                                } catch (SchematicHandler.UnsupportedFormatException e) {
                                    LOGGER.error(e);
                                }
                            });
                });
        return true;
    }

    private int get_ey(final HybridPlotWorld hpw, QueueCoordinator queue, int sx, int ex, int sz, int ez, int sy) {
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y <= hpw.getMaxGenHeight(); y++) {
                    if (y > ey) {
                        BlockState block = queue.getBlock(x, y, z);
                        if (!block.getBlockType().getMaterial().isAir()) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }

    /**
     * Regenerate the road in a chunk in a plot area.
     *
     * @param area             Plot area to regenerate road for
     * @param chunk            Chunk location to regenerate
     * @param extend           How far to extend setting air above the road
     * @param queueCoordinator {@link QueueCoordinator} to use to set the blocks. Null if one should be created and enqueued
     * @return if successful
     * @since 6.6.0
     */
    public boolean regenerateRoad(
            final PlotArea area,
            final BlockVector2 chunk,
            int extend,
            @Nullable QueueCoordinator queueCoordinator
    ) {
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int ex = x + 15;
        int ez = z + 15;
        HybridPlotWorld plotWorld = (HybridPlotWorld) area;
        if (!plotWorld.ROAD_SCHEMATIC_ENABLED) {
            return false;
        }
        AtomicBoolean toCheck = new AtomicBoolean(false);
        if (plotWorld.getType() == PlotAreaType.PARTIAL) {
            boolean chunk1 = area.contains(x, z);
            boolean chunk2 = area.contains(ex, ez);
            if (!chunk1 && !chunk2) {
                return false;
            } else {
                toCheck.set(chunk1 ^ chunk2);
            }
        }
        PlotManager manager = area.getPlotManager();
        PlotId id1 = manager.getPlotId(x, 0, z);
        PlotId id2 = manager.getPlotId(ex, 0, ez);
        x = x - plotWorld.ROAD_OFFSET_X;
        z -= plotWorld.ROAD_OFFSET_Z;
        final int finalX = x;
        final int finalZ = z;
        final boolean enqueue;
        final QueueCoordinator queue;
        if (queueCoordinator == null) {
            queue = this.blockQueue.getNewQueue(worldUtil.getWeWorld(plotWorld.getWorldName()));
            enqueue = true;
        } else {
            queue = queueCoordinator;
            enqueue = false;
        }
        if (id1 == null || id2 == null || id1 != id2) {
            if (id1 != null) {
                Plot p1 = area.getPlotAbs(id1);
                if (p1 != null && p1.hasOwner() && p1.isMerged()) {
                    toCheck.set(true);
                }
            }
            if (id2 != null && !toCheck.get()) {
                Plot p2 = area.getPlotAbs(id2);
                if (p2 != null && p2.hasOwner() && p2.isMerged()) {
                    toCheck.set(true);
                }
            }
            short size = plotWorld.SIZE;
            for (int X = 0; X < 16; X++) {
                short absX = (short) ((finalX + X) % size);
                for (int Z = 0; Z < 16; Z++) {
                    short absZ = (short) ((finalZ + Z) % size);
                    if (absX < 0) {
                        absX += size;
                    }
                    if (absZ < 0) {
                        absZ += size;
                    }
                    boolean condition;
                    if (toCheck.get()) {
                        condition = manager.getPlotId(
                                finalX + X + plotWorld.ROAD_OFFSET_X,
                                1,
                                finalZ + Z + plotWorld.ROAD_OFFSET_Z
                        ) == null;
                    } else {
                        boolean gx = absX > plotWorld.PATH_WIDTH_LOWER;
                        boolean gz = absZ > plotWorld.PATH_WIDTH_LOWER;
                        boolean lx = absX < plotWorld.PATH_WIDTH_UPPER;
                        boolean lz = absZ < plotWorld.PATH_WIDTH_UPPER;
                        condition = !gx || !gz || !lx || !lz;
                    }
                    if (condition) {
                        BaseBlock[] blocks = plotWorld.G_SCH.get(MathMan.pair(absX, absZ));
                        int minY = plotWorld.getRoadYStart();
                        int maxDy = Math.max(extend, blocks.length);
                        for (int dy = 0; dy < maxDy; dy++) {
                            if (dy > blocks.length - 1) {
                                queue.setBlock(
                                        finalX + X + plotWorld.ROAD_OFFSET_X,
                                        minY + dy,
                                        finalZ + Z + plotWorld.ROAD_OFFSET_Z,
                                        WEExtent.AIRBASE
                                );
                            } else {
                                BaseBlock block = blocks[dy];
                                if (block != null) {
                                    queue.setBlock(
                                            finalX + X + plotWorld.ROAD_OFFSET_X,
                                            minY + dy,
                                            finalZ + Z + plotWorld.ROAD_OFFSET_Z,
                                            block
                                    );
                                } else {
                                    queue.setBlock(
                                            finalX + X + plotWorld.ROAD_OFFSET_X,
                                            minY + dy,
                                            finalZ + Z + plotWorld.ROAD_OFFSET_Z,
                                            WEExtent.AIRBASE
                                    );
                                }
                            }
                        }
                        BiomeType biome = plotWorld.G_SCH_B.get(MathMan.pair(absX, absZ));
                        if (biome != null) {
                            queue.setBiome(finalX + X + plotWorld.ROAD_OFFSET_X, finalZ + Z + plotWorld.ROAD_OFFSET_Z, biome);
                        } else {
                            queue.setBiome(
                                    finalX + X + plotWorld.ROAD_OFFSET_X,
                                    finalZ + Z + plotWorld.ROAD_OFFSET_Z,
                                    plotWorld.getPlotBiome()
                            );
                        }
                    }
                }
            }
            if (enqueue) {
                queue.enqueue();
            }
            return true;
        }
        return false;
    }

}
