package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ChunkManager {

    private static final Map<ChunkLoc, RunnableVal<ScopedLocalBlockQueue>> forceChunks =
        new ConcurrentHashMap<>();
    private static final Map<ChunkLoc, RunnableVal<ScopedLocalBlockQueue>> addChunks =
        new ConcurrentHashMap<>();
    public static ChunkManager manager = null;

    public static ChunkLoc getChunkChunk(Location location) {
        int x = location.getX() >> 9;
        int z = location.getZ() >> 9;
        return new ChunkLoc(x, z);
    }

    public static void setChunkInPlotArea(RunnableVal<ScopedLocalBlockQueue> force,
        RunnableVal<ScopedLocalBlockQueue> add, String world, ChunkLoc loc) {
        LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
        if (PlotSquared.get().isAugmented(world)) {
            int bx = loc.x << 4;
            int bz = loc.z << 4;
            ScopedLocalBlockQueue scoped =
                new ScopedLocalBlockQueue(queue, new Location(world, bx, 0, bz),
                    new Location(world, bx + 15, 255, bz + 15));
            if (force != null) {
                force.run(scoped);
            } else {
                scoped.regenChunk(loc.x, loc.z);
                if (add != null) {
                    add.run(scoped);
                }
            }
            queue.flush();
        } else {
            forceChunks.put(loc, force);
            addChunks.put(loc, add);
            queue.regenChunk(loc.x, loc.z);
            forceChunks.remove(loc);
            addChunks.remove(loc);
        }
    }

    public static boolean preProcessChunk(ChunkLoc loc, ScopedLocalBlockQueue queue) {
        final RunnableVal<ScopedLocalBlockQueue> forceChunk = forceChunks.get(loc);
        if (forceChunk != null) {
            forceChunk.run(queue);
            forceChunks.remove(loc);
            return true;
        }
        return false;
    }

    public static boolean postProcessChunk(ChunkLoc loc, ScopedLocalBlockQueue queue) {
        final RunnableVal<ScopedLocalBlockQueue> addChunk = forceChunks.get(loc);
        if (addChunk != null) {
            addChunk.run(queue);
            addChunks.remove(loc);
            return true;
        }
        return false;
    }

    public static void largeRegionTask(final String world, final RegionWrapper region,
        final RunnableVal<ChunkLoc> task, final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            HashSet<ChunkLoc> chunks = new HashSet<>();
            Set<ChunkLoc> mcrs = manager.getChunkChunks(world);
            for (ChunkLoc mcr : mcrs) {
                int bx = mcr.x << 9;
                int bz = mcr.z << 9;
                int tx = bx + 511;
                int tz = bz + 511;
                if (bx <= region.maxX && tx >= region.minX && bz <= region.maxZ
                    && tz >= region.minZ) {
                    for (int x = bx >> 4; x <= (tx >> 4); x++) {
                        int cbx = x << 4;
                        int ctx = cbx + 15;
                        if (cbx <= region.maxX && ctx >= region.minX) {
                            for (int z = bz >> 4; z <= (tz >> 4); z++) {
                                int cbz = z << 4;
                                int ctz = cbz + 15;
                                if (cbz <= region.maxZ && ctz >= region.minZ) {
                                    chunks.add(new ChunkLoc(x, z));
                                }
                            }
                        }
                    }
                }
            }
            TaskManager.objectTask(chunks, new RunnableVal<ChunkLoc>() {

                @Override public void run(ChunkLoc value) {
                    if (manager.loadChunk(world, value, false)) {
                        task.run(value);
                    }
                }
            }, whenDone);
        });
    }

    public static void chunkTask(final Plot plot, final RunnableVal<int[]> task,
        final Runnable whenDone, final int allocate) {
        final ArrayList<RegionWrapper> regions = new ArrayList<>(plot.getRegions());
        Runnable smallTask = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper value = regions.remove(0);
                Location pos1 = new Location(plot.getWorldName(), value.minX, 0, value.minZ);
                Location pos2 = new Location(plot.getWorldName(), value.maxX, 0, value.maxZ);
                chunkTask(pos1, pos2, task, this, allocate);
            }
        };
        smallTask.run();
    }

    /**
     * The int[] will be in the form: [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge] and will represent the bottom and top parts of the chunk
     *
     * @param pos1
     * @param pos2
     * @param task
     * @param whenDone
     */
    public static void chunkTask(Location pos1, Location pos2, final RunnableVal<int[]> task,
        final Runnable whenDone, final int allocate) {
        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;
        final ArrayList<ChunkLoc> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && ((System.currentTimeMillis() - start) < allocate)) {
                    ChunkLoc chunk = chunks.remove(0);
                    task.value = new int[7];
                    task.value[0] = chunk.x;
                    task.value[1] = chunk.z;
                    task.value[2] = task.value[0] << 4;
                    task.value[3] = task.value[1] << 4;
                    task.value[4] = task.value[2] + 15;
                    task.value[5] = task.value[3] + 15;
                    if (task.value[0] == bcx) {
                        task.value[2] = p1x;
                        task.value[6] = 1;
                    }
                    if (task.value[0] == tcx) {
                        task.value[4] = p2x;
                        task.value[6] = 1;
                    }
                    if (task.value[1] == bcz) {
                        task.value[3] = p1z;
                        task.value[6] = 1;
                    }
                    if (task.value[1] == tcz) {
                        task.value[5] = p2z;
                        task.value[6] = 1;
                    }
                    task.run();
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }

    /**
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     *
     * @param plot
     * @return
     */
    public abstract int[] countEntities(Plot plot);

    public abstract boolean loadChunk(String world, ChunkLoc loc, boolean force);

    public abstract void unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe);

    public Set<ChunkLoc> getChunkChunks(String world) {
        File folder =
            new File(PlotSquared.get().IMP.getWorldContainer(), world + File.separator + "region");
        File[] regionFiles = folder.listFiles();
        HashSet<ChunkLoc> chunks = new HashSet<>();
        if (regionFiles == null) {
            throw new RuntimeException(
                "Could not find worlds folder: " + folder + " ? (no read access?)");
        }
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    int x = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    ChunkLoc loc = new ChunkLoc(x, z);
                    chunks.add(loc);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return chunks;
    }

    public void deleteRegionFiles(String world, Collection<ChunkLoc> chunks) {
        deleteRegionFiles(world, chunks, null);
    }

    public void deleteRegionFiles(final String world, final Collection<ChunkLoc> chunks,
        final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            for (ChunkLoc loc : chunks) {
                String directory =
                    world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z
                        + ".mca";
                File file = new File(PlotSquared.get().IMP.getWorldContainer(), directory);
                PlotSquared.log("&6 - Deleting file: " + file.getName() + " (max 1024 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
            TaskManager.runTask(whenDone);
        });
    }

    public Plot hasPlot(String world, ChunkLoc chunk) {
        int x1 = chunk.x << 4;
        int z1 = chunk.z << 4;
        int x2 = x1 + 15;
        int z2 = z1 + 15;
        Location bot = new Location(world, x1, 0, z1);
        Plot plot = bot.getOwnedPlotAbs();
        if (plot != null) {
            return plot;
        }
        Location top = new Location(world, x2, 0, z2);
        plot = top.getOwnedPlotAbs();
        return plot;
    }

    /**
     * Copy a region to a new location (in the same world)
     */
    public abstract boolean copyRegion(Location pos1, Location pos2, Location newPos,
        Runnable whenDone);

    /**
     * Assumptions:<br>
     * - pos1 and pos2 are in the same plot<br>
     * It can be harmful to the world if parameters outside this scope are provided
     *
     * @param pos1
     * @param pos2
     * @param whenDone
     * @return
     */
    public abstract boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment,
        Runnable whenDone);

    public abstract void clearAllEntities(Location pos1, Location pos2);

    public abstract void swap(Location bot1, Location top1, Location bot2, Location top2,
        Runnable whenDone);
}
