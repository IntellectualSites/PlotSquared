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
import com.plotsquared.core.queue.ChunkQueueCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HybridUtils {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + HybridUtils.class.getSimpleName());

    public static HybridUtils manager;
    public static Set<BlockVector2> regions;
    public static int height;
    public static Set<BlockVector2> chunks = new HashSet<>();
    public static PlotArea area;
    public static boolean UPDATE = false;

    private final PlotAreaManager plotAreaManager;
    private final ChunkManager chunkManager;
    private final GlobalBlockQueue blockQueue;
    private final WorldUtil worldUtil;
    private final RegionManager regionManager;
    private final SchematicHandler schematicHandler;

    @Inject public HybridUtils(@Nonnull final PlotAreaManager plotAreaManager,
        @Nonnull final ChunkManager chunkManager, @Nonnull final GlobalBlockQueue blockQueue,
        @Nonnull final WorldUtil worldUtil, @Nonnull final RegionManager regionManager, @Nonnull final SchematicHandler schematicHandler) {
        this.plotAreaManager = plotAreaManager;
        this.chunkManager = chunkManager;
        this.blockQueue = blockQueue;
        this.worldUtil = worldUtil;
        this.regionManager = regionManager;
        this.schematicHandler = schematicHandler;
    }

    public void regeneratePlotWalls(final PlotArea area) {
        PlotManager plotManager = area.getPlotManager();
        plotManager.regenerateAllPlotWalls();
    }

    public void analyzeRegion(final String world, final CuboidRegion region,
        final RunnableVal<PlotAnalysis> whenDone) {
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
            final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world), false);

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
            MainUtil.initCache();
            final int width = tx - bx + 1;
            final int length = tz - bz + 1;

            final PlotArea area = this.plotAreaManager.getPlotArea(world, null);

            if (!(area instanceof HybridPlotWorld)) {
                return;
            }

            HybridPlotWorld hpw = (HybridPlotWorld) area;
            ChunkQueueCoordinator chunk = new ChunkQueueCoordinator(bot, top, false);
            hpw.getGenerator().generateChunk(chunk, hpw);

            final BlockState[][][] oldBlocks = chunk.getBlocks();
            final BlockState[][][] newBlocks = new BlockState[256][width][length];
            final BlockState airBlock = BlockTypes.AIR.getDefaultState();

            System.gc();
            System.gc();

            final Runnable run = () -> TaskManager.runTaskAsync(() -> {
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
                        for (int y = 0; y < 256; y++) {
                            BlockState old = oldBlocks[y][x][z];
                            try {
                                if (old == null) {
                                    old = airBlock;
                                }
                                BlockState now = newBlocks[y][x][z];
                                if (!old.equals(now)) {
                                    changes[i]++;
                                }
                                if (now.getBlockType().getMaterial().isAir()) {
                                    air[i]++;
                                } else {
                                    // check vertices
                                    // modifications_adjacent
                                    if (x > 0 && z > 0 && y > 0 && x < width - 1 && z < length - 1
                                        && y < 255) {
                                        if (newBlocks[y - 1][x][z].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x - 1][z].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x][z - 1].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y + 1][x][z].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x + 1][z].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x][z + 1].getBlockType().getMaterial()
                                            .isAir()) {
                                            faces[i]++;
                                        }
                                    }

                                    if (!now.equals(now.getBlockType().getDefaultState())) {
                                        data[i]++;
                                    }
                                    types.add(now.getBlockType());
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
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
                System.gc();
                System.gc();
                whenDone.value = analysis;
                whenDone.run();
            });
            System.gc();
            MainUtil.initCache();
            Location botLoc = Location.at(world, bot.getX(), bot.getY(), bot.getZ());
            Location topLoc = Location.at(world, top.getX(), top.getY(), top.getZ());
            ChunkManager.chunkTask(botLoc, topLoc, new RunnableVal<int[]>() {
                @Override public void run(int[] value) {
                    int X = value[0];
                    int Z = value[1];
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
                        maxX = 16;
                    }
                    int maxZ;
                    if (Z == ctz) {
                        maxZ = tz & 15;
                    } else {
                        maxZ = 16;
                    }

                    int cbx = X << 4;
                    int cbz = Z << 4;

                    int xb = cbx - bx;
                    int zb = cbz - bz;
                    for (int x = minX; x <= maxX; x++) {
                        int xx = cbx + x;
                        for (int z = minZ; z <= maxZ; z++) {
                            int zz = cbz + z;
                            for (int y = 0; y < 256; y++) {
                                BlockState block = queue.getBlock(xx, y, zz);
                                int xr = xb + x;
                                int zr = zb + z;
                                newBlocks[y][xr][zr] = block;
                            }
                        }
                    }
                }
            }, () -> TaskManager.runTaskAsync(run), 5);
        });
    }

    public void analyzePlot(final Plot origin, final RunnableVal<PlotAnalysis> whenDone) {
        final ArrayDeque<CuboidRegion> zones = new ArrayDeque<>(origin.getRegions());
        final ArrayList<PlotAnalysis> analysis = new ArrayList<>();
        Runnable run = new Runnable() {
            @Override public void run() {
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
                    PlotFlag<?, ?> plotFlag =
                        GlobalFlagContainer.getInstance().getFlag(AnalysisFlag.class)
                            .createFlagInstance(result);
                    PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, origin);
                    if (event.getEventResult() == Result.DENY) {
                        return;
                    }
                    origin.setFlag(event.getFlag());
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = zones.poll();
                final Runnable task = this;
                analyzeRegion(origin.getWorldName(), region, new RunnableVal<PlotAnalysis>() {
                    @Override public void run(PlotAnalysis value) {
                        analysis.add(value);
                        TaskManager.runTaskLater(task, TaskTime.ticks(1L));
                    }
                });
            }
        };
        run.run();
    }

    public int checkModified(QueueCoordinator queue, int x1, int x2, int y1, int y2, int z1, int z2,
        BlockState[] blocks) {
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    BlockState block = queue.getBlock(x, y, z);
                    boolean same =
                        Arrays.stream(blocks).anyMatch(p -> this.worldUtil.isBlockSame(block, p));
                    if (!same) {
                        count++;
                    }
                }
            }
        }
        return count;
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
        Set<BlockVector2> regions = this.regionManager.getChunkChunks(area.getWorldName());
        return scheduleRoadUpdate(area, regions, extend, new HashSet<>());
    }

    public boolean scheduleSingleRegionRoadUpdate(Plot plot, int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        Set<BlockVector2> regions = new HashSet<>();
        regions.add(RegionManager.getRegion(plot.getCenterSynchronous()));
        return scheduleRoadUpdate(plot.getArea(), regions, extend, new HashSet<>());
    }

    public boolean scheduleRoadUpdate(final PlotArea area, Set<BlockVector2> regions,
        final int extend, Set<BlockVector2> chunks) {
        HybridUtils.regions = regions;
        HybridUtils.area = area;
        HybridUtils.height = extend;
        HybridUtils.chunks = chunks;
        final AtomicInteger count = new AtomicInteger(0);
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                if (!UPDATE) {
                    Iterator<BlockVector2> iter = chunks.iterator();
                    while (iter.hasNext()) {
                        BlockVector2 chunk = iter.next();
                        iter.remove();
                        boolean regenedRoad = regenerateRoad(area, chunk, extend);
                        if (!regenedRoad && Settings.DEBUG) {
                            logger.info("[P2] Failed to regenerate roads");
                        }
                        chunkManager.unloadChunk(area.getWorldName(), chunk, true);
                    }
                    if (Settings.DEBUG) {
                        logger.info("[P2] Cancelled road task");
                    }
                    return;
                }
                count.incrementAndGet();
                if (count.intValue() % 20 == 0) {
                    logger.info("[P2] Progress: {}%", 100 * (2048 - chunks.size()) / 2048);
                }
                if (HybridUtils.regions.isEmpty() && chunks.isEmpty()) {
                    regeneratePlotWalls(area);

                    HybridUtils.UPDATE = false;
                    logger.info("[P2] Finished road conversion");
                    // CANCEL TASK
                } else {
                    final Runnable task = this;
                    TaskManager.runTaskAsync(() -> {
                        try {
                            if (chunks.size() < 1024) {
                                if (!HybridUtils.regions.isEmpty()) {
                                    Iterator<BlockVector2> iterator =
                                        HybridUtils.regions.iterator();
                                    BlockVector2 loc = iterator.next();
                                    iterator.remove();
                                    if (Settings.DEBUG) {
                                        logger.info("[P2] Updating .mcr: {}, {} (approx 1024 chunks)",
                                            loc.getX(), loc.getZ());
                                        logger.info("[P2] - Remaining: {}", HybridUtils.regions.size());
                                    }
                                    chunks.addAll(getChunks(loc));
                                    System.gc();
                                }
                            }
                            if (!chunks.isEmpty()) {
                                TaskManager.getPlatformImplementation().sync(() -> {
                                    long start = System.currentTimeMillis();
                                    Iterator<BlockVector2> iterator = chunks.iterator();
                                    while (System.currentTimeMillis() - start < 20 && !chunks
                                        .isEmpty()) {
                                        final BlockVector2 chunk = iterator.next();
                                        iterator.remove();
                                        boolean regenedRoads =
                                            regenerateRoad(area, chunk, extend);
                                        if (!regenedRoads && Settings.DEBUG) {
                                            logger.info("[P2] Failed to regenerate road");
                                        }
                                    }
                                    return null;
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Iterator<BlockVector2> iterator = HybridUtils.regions.iterator();
                            BlockVector2 loc = iterator.next();
                            iterator.remove();
                            logger.error("[P2] Error! Could not update '{}/region/r.{}.{}.mca' (Corrupt chunk?)",
                                area.getWorldHash(), loc.getX(), loc.getZ());
                            int sx = loc.getX() << 5;
                            int sz = loc.getZ() << 5;
                            for (int x = sx; x < sx + 32; x++) {
                                for (int z = sz; z < sz + 32; z++) {
                                    chunkManager.unloadChunk(area.getWorldName(), BlockVector2.at(x, z),
                                            true);
                                }
                            }
                        }
                        blockQueue.addEmptyTask(() -> TaskManager.runTaskLater(task, TaskTime.seconds(1L)));
                    });
                }
            }
        });
        return true;
    }

    public boolean setupRoadSchematic(Plot plot) {
        final String world = plot.getWorldName();
        final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world), false);
        Location bot = plot.getBottomAbs().subtract(1, 0, 1);
        Location top = plot.getTopAbs();
        final HybridPlotWorld plotworld = (HybridPlotWorld) plot.getArea();
        PlotManager plotManager = plotworld.getPlotManager();
        int sx = bot.getX() - plotworld.ROAD_WIDTH + 1;
        int sz = bot.getZ() + 1;
        int sy = Settings.Schematics.PASTE_ROAD_ON_TOP ? plotworld.ROAD_HEIGHT : 1;
        int ex = bot.getX();
        int ez = top.getZ();
        int ey = get_ey(plotManager, queue, sx, ex, sz, ez, sy);
        int bz = sz - plotworld.ROAD_WIDTH;
        int tz = sz - 1;
        int ty = get_ey(plotManager, queue, sx, ex, bz, tz, sy);

        Set<CuboidRegion> sideRoad = new HashSet<>(
            Collections.singletonList(RegionUtil.createRegion(sx, ex, sy, ey, sz, ez)));
        final Set<CuboidRegion> intersection = new HashSet<>(
            Collections.singletonList(RegionUtil.createRegion(sx, ex, sy, ty, bz, tz)));

        final String dir =
            "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.getArea()
                .toString() + File.separator;

        this.schematicHandler.getCompoundTag(world, sideRoad, new RunnableVal<CompoundTag>() {
            @Override public void run(CompoundTag value) {
                schematicHandler.save(value, dir + "sideroad.schem");
                schematicHandler.getCompoundTag(world, intersection, new RunnableVal<CompoundTag>() {
                        @Override public void run(CompoundTag value) {
                            schematicHandler.save(value, dir + "intersection.schem");
                            plotworld.ROAD_SCHEMATIC_ENABLED = true;
                            try {
                                plotworld.setupSchematics();
                            } catch (SchematicHandler.UnsupportedFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }
        });
        return true;
    }

    public int get_ey(final PlotManager pm, QueueCoordinator queue, int sx, int ex, int sz, int ez,
        int sy) {
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y <= pm.getWorldHeight(); y++) {
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

    public boolean regenerateRoad(final PlotArea area, final BlockVector2 chunk, int extend) {
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
        QueueCoordinator queue = this.blockQueue.getNewQueue(worldUtil.getWeWorld(plotWorld.getWorldName()), false);
        if (id1 == null || id2 == null || id1 != id2) {
            this.chunkManager.loadChunk(area.getWorldName(), chunk, false).thenRun(() -> {
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
                int size = plotWorld.SIZE;
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
                            condition = manager.getPlotId(finalX + X + plotWorld.ROAD_OFFSET_X, 1,
                                finalZ + Z + plotWorld.ROAD_OFFSET_Z) == null;
                        } else {
                            boolean gx = absX > plotWorld.PATH_WIDTH_LOWER;
                            boolean gz = absZ > plotWorld.PATH_WIDTH_LOWER;
                            boolean lx = absX < plotWorld.PATH_WIDTH_UPPER;
                            boolean lz = absZ < plotWorld.PATH_WIDTH_UPPER;
                            condition = !gx || !gz || !lx || !lz;
                        }
                        if (condition) {
                            BaseBlock[] blocks = plotWorld.G_SCH.get(MathMan.pair(absX, absZ));
                            int minY = Settings.Schematics.PASTE_ROAD_ON_TOP ? plotWorld.SCHEM_Y : 1;
                            int maxY = Math.max(extend, blocks.length);
                            for (int y = 0; y < maxY; y++) {
                                if (y > blocks.length - 1) {
                                    queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X, minY + y,
                                        finalZ + Z + plotWorld.ROAD_OFFSET_Z, WEExtent.AIRBASE);
                                } else {
                                    BaseBlock block = blocks[y];
                                    if (block != null) {
                                        queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X,
                                            minY + y, finalZ + Z + plotWorld.ROAD_OFFSET_Z, block);
                                    } else {
                                        queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X,
                                            minY + y, finalZ + Z + plotWorld.ROAD_OFFSET_Z,
                                            WEExtent.AIRBASE);
                                    }
                                }
                            }
                            BiomeType biome = plotWorld.G_SCH_B.get(MathMan.pair(absX, absZ));
                            if (biome != null) {
                                queue.setBiome(finalX + X + plotWorld.ROAD_OFFSET_X,
                                    finalZ + Z + plotWorld.ROAD_OFFSET_Z, biome);
                            } else {
                                queue.setBiome(finalX + X + plotWorld.ROAD_OFFSET_X,
                                    finalZ + Z + plotWorld.ROAD_OFFSET_Z, plotWorld.getPlotBiome());
                            }
                        }
                    }
                }
                queue.enqueue();
            });
            return true;
        }
        return false;
    }
}
