package com.intellectualcrafters.plot.generator;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;

public abstract class HybridUtils {
    
    public static HybridUtils manager;
    
    public abstract void analyzeRegion(final String world, final RegionWrapper region, final RunnableVal<PlotAnalysis> whenDone);
    
    public void analyzePlot(final Plot origin, final RunnableVal<PlotAnalysis> whenDone) {
        final ArrayDeque<RegionWrapper> zones = new ArrayDeque<>(MainUtil.getRegions(origin));
        final ArrayList<PlotAnalysis> analysis = new ArrayList<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (zones.size() == 0) {
                    if (analysis.size() > 0) {
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
                    }
                    else {
                        whenDone.value = analysis.get(0);
                    }
                    final List<Integer> result = new ArrayList<>();
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
                    final Flag flag = new Flag(FlagManager.getFlag("analysis"), result);
                    FlagManager.addPlotFlag(origin, flag);
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = zones.poll();
                final Runnable task = this;
                analyzeRegion(origin.world, region, new RunnableVal<PlotAnalysis>() {
                    @Override
                    public void run() {
                        analysis.add(value);
                        TaskManager.runTaskLater(task, 1);
                    }
                });
            }
        };
        run.run();
    }
    
    public abstract int checkModified(final String world, final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks);
    
    public static Set<ChunkLoc> regions;
    public static Set<ChunkLoc> chunks = new HashSet<>();
    public static String world;
    public static boolean UPDATE = false;
    
    public final ArrayList<ChunkLoc> getChunks(final ChunkLoc region) {
        final ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
        final int sx = region.x << 5;
        final int sz = region.z << 5;
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        return chunks;
    }
    
    /**
     * Checks all connected plots
     * @param plot
     * @param whenDone
     */
    public void checkModified(final Plot plot, final RunnableVal<Integer> whenDone) {
        if (whenDone == null) {
            return;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (!(plotworld instanceof ClassicPlotWorld)) {
            whenDone.value = -1;
            TaskManager.runTask(whenDone);
            return;
        }
        whenDone.value = 0;
        final ClassicPlotWorld cpw = (ClassicPlotWorld) plotworld;
        final ArrayDeque<RegionWrapper> zones = new ArrayDeque<>(MainUtil.getRegions(plot));
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (zones.size() == 0) {
                    
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = zones.poll();
                Location pos1 = new Location(plot.world, region.minX, region.minY, region.minZ);
                Location pos2 = new Location(plot.world, region.maxX, region.maxY, region.maxZ);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override
                    public void run() {
                        final ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                        ChunkManager.manager.loadChunk(plot.world, loc, false);
                        final int bx = value[2];
                        final int bz = value[3];
                        final int ex = value[4];
                        final int ez = value[5];
                        whenDone.value += checkModified(plot.world, bx, ex, 1, cpw.PLOT_HEIGHT - 1, bz, ez, cpw.MAIN_BLOCK);
                        whenDone.value += checkModified(plot.world, bx, ex, cpw.PLOT_HEIGHT, cpw.PLOT_HEIGHT, bz, ez, cpw.TOP_BLOCK);
                        whenDone.value += checkModified(plot.world, bx, ex, cpw.PLOT_HEIGHT + 1, 255, bz, ez, new PlotBlock[] { new PlotBlock((short) 0, (byte) 0) });
                    }
                }, this, 5);
                
            }
        };
        run.run();
    }
    
    public boolean scheduleRoadUpdate(final String world, final int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        final Set<ChunkLoc> regions = ChunkManager.manager.getChunkChunks(world);
        return scheduleRoadUpdate(world, regions, extend);
    }
    
    public boolean scheduleRoadUpdate(final String world, final Set<ChunkLoc> rgs, final int extend) {
        HybridUtils.regions = rgs;
        HybridUtils.world = world;
        chunks = new HashSet<ChunkLoc>();
        final AtomicInteger count = new AtomicInteger(0);
        final long baseTime = System.currentTimeMillis();
        final AtomicInteger last = new AtomicInteger();
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                if (UPDATE == false) {
                    last.set(0);
                    Iterator<ChunkLoc> iter = chunks.iterator();
                    while (iter.hasNext()) {
                        final ChunkLoc chunk = iter.next();
                        iter.remove();
                        regenerateRoad(world, chunk, extend);
                        ChunkManager.manager.unloadChunk(world, chunk, true, true);
                    }
                    PS.debug("&cCancelled road task");
                    return;
                }
                count.incrementAndGet();
                if ((count.intValue() % 20) == 0) {
                    PS.debug("PROGRESS: " + ((100 * (2048 - chunks.size())) / 2048) + "%");
                }
                if ((regions.size() == 0) && (chunks.size() == 0)) {
                    HybridUtils.UPDATE = false;
                    PS.debug(C.PREFIX.s() + "Finished road conversion");
                    // CANCEL TASK
                    return;
                } else {
                    final Runnable task = this;
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (last.get() == 0) {
                                    last.set((int) (System.currentTimeMillis() - baseTime));
                                }
                                if (chunks.size() < 1024) {
                                    if (regions.size() > 0) {
                                        Iterator<ChunkLoc> iter = regions.iterator();
                                        final ChunkLoc loc = iter.next();
                                        iter.remove();
                                        PS.debug("&3Updating .mcr: " + loc.x + ", " + loc.z + " (aprrox 1024 chunks)");
                                        PS.debug(" - Remaining: " + regions.size());
                                        chunks.addAll(getChunks(loc));
                                        System.gc();
                                    }
                                }
                                if (chunks.size() > 0) {
                                    final long diff = System.currentTimeMillis() + 1;
                                    if (((System.currentTimeMillis() - baseTime - last.get()) > 2000) && (last.get() != 0)) {
                                        last.set(0);
                                        PS.debug(C.PREFIX.s() + "Detected low TPS. Rescheduling in 30s");
                                        Iterator<ChunkLoc> iter = chunks.iterator();
                                        final ChunkLoc chunk = iter.next();
                                        iter.remove();
                                        TaskManager.runTask(new Runnable() {
                                            @Override
                                            public void run() {
                                                regenerateRoad(world, chunk, extend);
                                            }
                                        });
                                        // DELAY TASK
                                        TaskManager.runTaskLater(task, 600);
                                        return;
                                    }
                                    if ((((System.currentTimeMillis() - baseTime) - last.get()) < 1500) && (last.get() != 0)) {
                                        while ((System.currentTimeMillis() < diff) && (chunks.size() > 0)) {
                                            Iterator<ChunkLoc> iter = chunks.iterator();
                                            final ChunkLoc chunk = iter.next();
                                            iter.remove();
                                            TaskManager.runTask(new Runnable() {
                                                @Override
                                                public void run() {
                                                    regenerateRoad(world, chunk, extend);
                                                }
                                            });
                                        }
                                    }
                                    last.set((int) (System.currentTimeMillis() - baseTime));
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                                Iterator<ChunkLoc> iter = regions.iterator();
                                final ChunkLoc loc = iter.next();
                                iter.remove();
                                PS.debug("&c[ERROR]&7 Could not update '" + world + "/region/r." + loc.x + "." + loc.z + ".mca' (Corrupt chunk?)");
                                final int sx = loc.x << 5;
                                final int sz = loc.z << 5;
                                for (int x = sx; x < (sx + 32); x++) {
                                    for (int z = sz; z < (sz + 32); z++) {
                                        ChunkManager.manager.unloadChunk(world, new ChunkLoc(x, z), true, true);
                                    }
                                }
                                PS.debug("&d - Potentially skipping 1024 chunks");
                                PS.debug("&d - TODO: recommend chunkster if corrupt");
                            }
                            SetBlockQueue.addNotify(new Runnable() {
                                @Override
                                public void run() {
                                    TaskManager.runTaskLater(task, 20);
                                }
                            });
                        }
                    });
                }
            }
        });
        return true;
    }
    
    public boolean setupRoadSchematic(final Plot plot) {
        final String world = plot.world;
        final Location bot = MainUtil.getPlotBottomLocAbs(world, plot.id).subtract(1, 0, 1);
        final Location top = MainUtil.getPlotTopLocAbs(world, plot.id);
        final HybridPlotWorld plotworld = (HybridPlotWorld) PS.get().getPlotWorld(world);
        final int sx = (bot.getX() - plotworld.ROAD_WIDTH) + 1;
        final int sz = bot.getZ() + 1;
        final int sy = plotworld.ROAD_HEIGHT;
        final int ex = bot.getX();
        final int ez = top.getZ();
        final int ey = get_ey(world, sx, ex, sz, ez, sy);
        final Location pos1 = new Location(world, sx, sy, sz);
        final Location pos2 = new Location(world, ex, ey, ez);
        final int bx = sx;
        final int bz = sz - plotworld.ROAD_WIDTH;
        final int by = sy;
        final int tx = ex;
        final int tz = sz - 1;
        final int ty = get_ey(world, bx, tx, bz, tz, by);
        
        final Set<RegionWrapper> sideroad = new HashSet<>(Arrays.asList(new RegionWrapper(sx, ex, sy, ey, sz, ez)));
        final Set<RegionWrapper> intersection = new HashSet<>(Arrays.asList(new RegionWrapper(bx, tx, by, ty, bz, tz)));
        
        final Location pos3 = new Location(world, bx, by, bz);
        final Location pos4 = new Location(world, tx, ty, tz);
        final String dir = PS.get().IMP.getDirectory() + File.separator + "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.world + File.separator;
        SchematicHandler.manager.getCompoundTag(world, sideroad, new RunnableVal<CompoundTag>() {
            @Override
            public void run() {
                SchematicHandler.manager.save(value, dir + "sideroad.schematic");
                SchematicHandler.manager.getCompoundTag(world, intersection, new RunnableVal<CompoundTag>() {
                    @Override
                    public void run() {
                        SchematicHandler.manager.save(value, dir + "intersection.schematic");
                        plotworld.ROAD_SCHEMATIC_ENABLED = true;
                        plotworld.setupSchematics();
                    }
                });
            }
        });
        return true;
    }
    
    public abstract int get_ey(final String world, final int sx, final int ex, final int sz, final int ez, final int sy);
    
    public boolean regenerateRoad(final String world, final ChunkLoc chunk, int extend) {
        int x = chunk.x << 4;
        int z = chunk.z << 4;
        final int ex = x + 15;
        final int ez = z + 15;
        final HybridPlotWorld plotworld = (HybridPlotWorld) PS.get().getPlotWorld(world);
        extend = Math.min(extend, 255 - plotworld.ROAD_HEIGHT - plotworld.SCHEMATIC_HEIGHT);
        if (!plotworld.ROAD_SCHEMATIC_ENABLED) {
            return false;
        }
        boolean toCheck = false;
        if (plotworld.TYPE == 2) {
            final boolean c1 = MainUtil.isPlotArea(new Location(plotworld.worldname, x, 1, z));
            final boolean c2 = MainUtil.isPlotArea(new Location(plotworld.worldname, ex, 1, ez));
            if (!c1 && !c2) {
                return false;
            } else {
                toCheck = c1 ^ c2;
            }
        }
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotId id1 = manager.getPlotId(plotworld, x, 0, z);
        final PlotId id2 = manager.getPlotId(plotworld, ex, 0, ez);
        x -= plotworld.ROAD_OFFSET_X;
        z -= plotworld.ROAD_OFFSET_Z;
        if ((id1 == null) || (id2 == null) || (id1 != id2)) {
            final boolean result = ChunkManager.manager.loadChunk(world, chunk, false);
            if (result) {
                if (id1 != null) {
                    final Plot p1 = MainUtil.getPlotAbs(world, id1);
                    if ((p1 != null) && p1.hasOwner() && p1.isMerged()) {
                        toCheck = true;
                    }
                }
                if ((id2 != null) && !toCheck) {
                    final Plot p2 = MainUtil.getPlotAbs(world, id2);
                    if ((p2 != null) && p2.hasOwner() && p2.isMerged()) {
                        toCheck = true;
                    }
                }
                final int size = plotworld.SIZE;
                for (int X = 0; X < 16; X++) {
                    short absX = (short) ((x + X) % size);
                    for (int Z = 0; Z < 16; Z++) {
                        short absZ = (short) ((z + Z) % size);
                        if (absX < 0) {
                            absX += size;
                        }
                        if (absZ < 0) {
                            absZ += size;
                        }
                        boolean condition;
                        if (toCheck) {
                            condition = manager.getPlotId(plotworld, x + X + plotworld.ROAD_OFFSET_X, 1, z + Z + plotworld.ROAD_OFFSET_Z) == null;
                            //                            condition = MainUtil.isPlotRoad(new Location(plotworld.worldname, x + X, 1, z + Z));
                        } else {
                            final boolean gx = absX > plotworld.PATH_WIDTH_LOWER;
                            final boolean gz = absZ > plotworld.PATH_WIDTH_LOWER;
                            final boolean lx = absX < plotworld.PATH_WIDTH_UPPER;
                            final boolean lz = absZ < plotworld.PATH_WIDTH_UPPER;
                            condition = (!gx || !gz || !lx || !lz);
                        }
                        if (condition) {
                            final int sy = plotworld.ROAD_HEIGHT;
                            final PlotLoc loc = new PlotLoc(absX, absZ);
                            final HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                            for (short y = (short) (plotworld.ROAD_HEIGHT); y <= (plotworld.ROAD_HEIGHT + plotworld.SCHEMATIC_HEIGHT + extend); y++) {
                                SetBlockQueue.setBlock(world, x + X + plotworld.ROAD_OFFSET_X, y, z + Z + plotworld.ROAD_OFFSET_Z, 0);
                            }
                            if (blocks != null) {
                                final HashMap<Short, Byte> datas = plotworld.G_SCH_DATA.get(loc);
                                if (datas == null) {
                                    for (final Short y : blocks.keySet()) {
                                        SetBlockQueue.setBlock(world, x + X + plotworld.ROAD_OFFSET_X, sy + y, z + Z + plotworld.ROAD_OFFSET_Z, blocks.get(y));
                                    }
                                } else {
                                    for (final Short y : blocks.keySet()) {
                                        Byte data = datas.get(y);
                                        if (data == null) {
                                            data = 0;
                                        }
                                        SetBlockQueue.setBlock(world, x + X + plotworld.ROAD_OFFSET_X, sy + y, z + Z + plotworld.ROAD_OFFSET_Z, new PlotBlock(blocks.get(y), data));
                                    }
                                }
                            }
                        }
                    }
                }
                SetBlockQueue.addNotify(new Runnable() {
                    @Override
                    public void run() {
                        ChunkManager.manager.unloadChunk(world, chunk, true, true);
                    }
                });
                return true;
            }
        }
        return false;
    }
}
