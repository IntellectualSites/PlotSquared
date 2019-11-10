package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.listener.WEExtent;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.util.world.RegionUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

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

public abstract class HybridUtils {

    public static HybridUtils manager;
    public static Set<BlockVector2> regions;
    public static Set<BlockVector2> chunks = new HashSet<>();
    public static PlotArea area;
    public static boolean UPDATE = false;

    public abstract void analyzeRegion(String world, CuboidRegion region,
        RunnableVal<PlotAnalysis> whenDone);

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
                    FlagManager.addPlotFlag(origin, Flags.ANALYSIS, result);
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = zones.poll();
                final Runnable task = this;
                analyzeRegion(origin.getWorldName(), region, new RunnableVal<PlotAnalysis>() {
                    @Override public void run(PlotAnalysis value) {
                        analysis.add(value);
                        TaskManager.runTaskLater(task, 1);
                    }
                });
            }
        };
        run.run();
    }

    public int checkModified(LocalBlockQueue queue, int x1, int x2, int y1, int y2, int z1, int z2,
        BlockState[] blocks) {
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    BlockState block = queue.getBlock(x, y, z);
                    boolean same =
                        Arrays.stream(blocks).anyMatch(p -> WorldUtil.IMP.isBlockSame(block, p));
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
        Set<BlockVector2> regions = ChunkManager.manager.getChunkChunks(area.worldname);
        return scheduleRoadUpdate(area, regions, extend);
    }

    public boolean scheduleSingleRegionRoadUpdate(Plot plot, int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        Set<BlockVector2> regions = new HashSet<>();
        regions.add(ChunkManager.getRegion(plot.getCenter()));
        return scheduleRoadUpdate(plot.getArea(), regions, extend);
    }

    public boolean scheduleRoadUpdate(final PlotArea area, Set<BlockVector2> regions,
        final int extend) {
        HybridUtils.regions = regions;
        HybridUtils.area = area;
        chunks = new HashSet<>();
        final AtomicInteger count = new AtomicInteger(0);
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                if (!UPDATE) {
                    Iterator<BlockVector2> iter = chunks.iterator();
                    while (iter.hasNext()) {
                        BlockVector2 chunk = iter.next();
                        iter.remove();
                        boolean regenedRoad = regenerateRoad(area, chunk, extend);
                        if (!regenedRoad) {
                            PlotSquared.debug("Failed to regenerate roads.");
                        }
                        ChunkManager.manager.unloadChunk(area.worldname, chunk, true);
                    }
                    PlotSquared.debug("Cancelled road task");
                    return;
                }
                count.incrementAndGet();
                if (count.intValue() % 20 == 0) {
                    PlotSquared.debug("PROGRESS: " + 100 * (2048 - chunks.size()) / 2048 + "%");
                }
                if (HybridUtils.regions.isEmpty() && chunks.isEmpty()) {
                    PlotSquared.debug("Regenerating plot walls");
                    regeneratePlotWalls(area);

                    HybridUtils.UPDATE = false;
                    PlotSquared.debug("Finished road conversion");
                    // CANCEL TASK
                } else {
                    final Runnable task = this;
                    TaskManager.runTaskAsync(() -> {
                        try {
                            if (chunks.size() < 1024) {
                                if (!HybridUtils.regions.isEmpty()) {
                                    Iterator<BlockVector2> iterator = HybridUtils.regions.iterator();
                                    BlockVector2 loc = iterator.next();
                                    iterator.remove();
                                    PlotSquared.debug("Updating .mcr: " + loc.getX() + ", " + loc.getZ()
                                        + " (approx 1024 chunks)");
                                    PlotSquared
                                        .debug(" - Remaining: " + HybridUtils.regions.size());
                                    chunks.addAll(getChunks(loc));
                                    System.gc();
                                }
                            }
                            if (!chunks.isEmpty()) {
                                TaskManager.IMP.sync(new RunnableVal<Object>() {
                                    @Override public void run(Object value) {
                                        long start = System.currentTimeMillis();
                                        Iterator<BlockVector2> iterator = chunks.iterator();
                                        while (System.currentTimeMillis() - start < 20 && !chunks
                                            .isEmpty()) {
                                            final BlockVector2 chunk = iterator.next();
                                            iterator.remove();
                                            boolean regenedRoads =
                                                regenerateRoad(area, chunk, extend);
                                            if (!regenedRoads) {
                                                PlotSquared.debug("Failed to regenerate road.");
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Iterator<BlockVector2> iterator = HybridUtils.regions.iterator();
                            BlockVector2 loc = iterator.next();
                            iterator.remove();
                            PlotSquared.debug(
                                "[ERROR] Could not update '" + area.worldname + "/region/r." + loc.getX()
                                    + "." + loc.getZ() + ".mca' (Corrupt chunk?)");
                            int sx = loc.getX() << 5;
                            int sz = loc.getZ() << 5;
                            for (int x = sx; x < sx + 32; x++) {
                                for (int z = sz; z < sz + 32; z++) {
                                    ChunkManager.manager
                                        .unloadChunk(area.worldname, BlockVector2.at(x, z), true);
                                }
                            }
                            PlotSquared.debug(" - Potentially skipping 1024 chunks");
                            PlotSquared.debug(" - TODO: recommend chunkster if corrupt");
                        }
                        GlobalBlockQueue.IMP.addEmptyTask(() -> TaskManager.runTaskLater(task, 20));
                    });
                }
            }
        });
        return true;
    }

    public boolean setupRoadSchematic(Plot plot) {
        final String world = plot.getWorldName();
        final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
        Location bot = plot.getBottomAbs().subtract(1, 0, 1);
        Location top = plot.getTopAbs();
        final HybridPlotWorld plotworld = (HybridPlotWorld) plot.getArea();
        PlotManager plotManager = plotworld.getPlotManager();
        int sx = bot.getX() - plotworld.ROAD_WIDTH + 1;
        int sz = bot.getZ() + 1;
        int sy = plotworld.ROAD_HEIGHT;
        int ex = bot.getX();
        int ez = top.getZ();
        int ey = get_ey(plotManager, queue, sx, ex, sz, ez, sy);
        int bz = sz - plotworld.ROAD_WIDTH;
        int tz = sz - 1;
        int ty = get_ey(plotManager, queue, sx, ex, bz, tz, sy);

        Set<CuboidRegion> sideRoad =
            new HashSet<>(Collections.singletonList(RegionUtil.createRegion(sx, ex, sy, ey, sz, ez)));
        final Set<CuboidRegion> intersection =
            new HashSet<>(Collections.singletonList(RegionUtil.createRegion(sx, ex, sy, ty, bz, tz)));

        final String dir =
            "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.getArea()
                .toString() + File.separator;

        SchematicHandler.manager.getCompoundTag(world, sideRoad, new RunnableVal<CompoundTag>() {
            @Override public void run(CompoundTag value) {
                SchematicHandler.manager.save(value, dir + "sideroad.schem");
                SchematicHandler.manager
                    .getCompoundTag(world, intersection, new RunnableVal<CompoundTag>() {
                        @Override public void run(CompoundTag value) {
                            SchematicHandler.manager.save(value, dir + "intersection.schem");
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

    public int get_ey(final PlotManager pm, LocalBlockQueue queue, int sx, int ex, int sz, int ez,
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
        if (plotWorld.TYPE == 2) {
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
        LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(plotWorld.worldname, false);
        if (id1 == null || id2 == null || id1 != id2) {
            ChunkManager.manager.loadChunk(area.worldname, chunk, false).thenRun(() -> {
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
                            int minY = plotWorld.SCHEM_Y;
                            if (!Settings.Schematics.PASTE_ON_TOP) {
                                minY = 1;
                            }
                            int maxY = Math.max(extend, blocks.length);
                            for (int y = 0; y < maxY; y++) {
                                if (y > blocks.length - 1) {
                                    queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X, minY + y,
                                        finalZ + Z + plotWorld.ROAD_OFFSET_Z, WEExtent.AIRBASE);
                                } else {
                                    BaseBlock block = blocks[y];
                                    if (block != null) {
                                        queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X, minY + y,
                                            finalZ + Z + plotWorld.ROAD_OFFSET_Z, block);
                                    } else {
                                        queue.setBlock(finalX + X + plotWorld.ROAD_OFFSET_X, minY + y,
                                            finalZ + Z + plotWorld.ROAD_OFFSET_Z, WEExtent.AIRBASE);
                                    }
                                }
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

    public boolean regeneratePlotWalls(final PlotArea area) {
        PlotManager plotManager = area.getPlotManager();
        return plotManager.regenerateAllPlotWalls();
    }
}
