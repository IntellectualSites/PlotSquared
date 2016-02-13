package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

public abstract class ChunkManager {
    
    public static ChunkManager manager = null;
    
    public static ChunkLoc getChunkChunk(final Location loc) {
        final int x = loc.getX() >> 9;
        final int z = loc.getZ() >> 9;
        return new ChunkLoc(x, z);
    }
    
    private static RunnableVal<PlotChunk<?>> CURRENT_FORCE_CHUNK;
    private static RunnableVal<PlotChunk<?>> CURRENT_ADD_CHUNK;
    
    public static void setChunkInPlotArea(RunnableVal<PlotChunk<?>> force, RunnableVal<PlotChunk<?>> add, String world, ChunkLoc loc) {
        if (PS.get().isAugmented(world)) {
            ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            PlotChunk<?> chunk = SetQueue.IMP.queue.getChunk(wrap);
            if (force != null) {
                force.run(chunk);
            }
            manager.regenerateChunk(world, loc);
            if (add != null) {
                add.run(chunk);
            }
            chunk.addToQueue();
            chunk.flush(true);
        } else {
            CURRENT_FORCE_CHUNK = force;
            CURRENT_ADD_CHUNK = add;
            manager.regenerateChunk(world, loc);
            CURRENT_FORCE_CHUNK = null;
            CURRENT_ADD_CHUNK = null;
        }
    }
    
    public static void preProcessChunk(PlotChunk<?> chunk) {
        if (CURRENT_FORCE_CHUNK != null) {
            CURRENT_FORCE_CHUNK.run(chunk);
            CURRENT_FORCE_CHUNK = null;
        }
    }
    
    public static void postProcessChunk(PlotChunk<?> chunk) {
        if (CURRENT_ADD_CHUNK != null) {
            CURRENT_ADD_CHUNK.run(chunk);
            CURRENT_ADD_CHUNK = null;
        }
    }

    public static void largeRegionTask(final String world, final RegionWrapper region, final RunnableVal<ChunkLoc> task, final Runnable whenDone) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                HashSet<ChunkLoc> chunks = new HashSet<>();
                final Set<ChunkLoc> mcrs = manager.getChunkChunks(world);
                for (ChunkLoc mcr : mcrs) {
                    int bx = mcr.x << 9;
                    int bz = mcr.z << 9;
                    int tx = bx + 511;
                    int tz = bz + 511;
                    if (bx <= region.maxX && tx >= region.minX && bz <= region.maxZ && tz >= region.minZ) {
                        for (int x = (bx >> 4); x <= (tx >> 4); x++) {
                            int cbx = x << 4;
                            int ctx = cbx + 15;
                            if (cbx <= region.maxX && ctx >= region.minX) {
                                for (int z = (bz >> 4); z <= (tz >> 4); z++) {
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
                    
                    @Override
                    public void run(ChunkLoc value) {
                        if (manager.loadChunk(world, value, false)) {
                            task.run(value);
                        }
                    }
                }, whenDone);
            }
        });
    }

    /**
     * The int[] will be in the form: [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge] and will represent the bottom and top parts of the chunk
     * @param pos1
     * @param pos2
     * @param task
     * @param whenDone
     */
    public static void chunkTask(final Location pos1, final Location pos2, final RunnableVal<int[]> task, final Runnable whenDone, final int allocate) {
        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;
        final ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
        
        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((chunks.size() > 0) && ((System.currentTimeMillis() - start) < allocate)) {
                    final ChunkLoc chunk = chunks.remove(0);
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
                if (chunks.size() != 0) {
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
     * @param plot
     * @return
     */
    public abstract int[] countEntities(final Plot plot);
    
    public abstract boolean loadChunk(final String world, final ChunkLoc loc, final boolean force);
    
    public abstract void unloadChunk(final String world, final ChunkLoc loc, final boolean save, final boolean safe);
    
    public Set<ChunkLoc> getChunkChunks(final String world) {
        final File folder = new File(PS.get().IMP.getWorldContainer(), world + File.separator + "region");
        final File[] regionFiles = folder.listFiles();
        final HashSet<ChunkLoc> chunks = new HashSet<>();
        if (regionFiles == null) {
            throw new RuntimeException("Could not find worlds folder: " + folder + " ? (no read access?)");
        }
        for (final File file : regionFiles) {
            final String name = file.getName();
            if (name.endsWith("mca")) {
                final String[] split = name.split("\\.");
                try {
                    final int x = Integer.parseInt(split[1]);
                    final int z = Integer.parseInt(split[2]);
                    final ChunkLoc loc = new ChunkLoc(x, z);
                    chunks.add(loc);
                } catch (final Exception e) {}
            }
        }
        return chunks;
    }
    
    public abstract void regenerateChunk(final String world, final ChunkLoc loc);
    
    public void deleteRegionFiles(String world, List<ChunkLoc> chunks) {
        deleteRegionFiles(world, chunks, null);
    }
    
    public void deleteRegionFiles(final String world, final List<ChunkLoc> chunks, final Runnable whenDone) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                for (final ChunkLoc loc : chunks) {
                    final String directory = world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z + ".mca";
                    final File file = new File(PS.get().IMP.getWorldContainer(), directory);
                    ConsolePlayer.getConsole().sendMessage("&6 - Deleting file: " + file.getName() + " (max 1024 chunks)");
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (whenDone != null) {
                    whenDone.run();
                }
            }
        });
    }
    
    public Plot hasPlot(String world, ChunkLoc chunk) {
        final int x1 = chunk.x << 4;
        final int z1 = chunk.z << 4;
        final int x2 = x1 + 15;
        final int z2 = z1 + 15;
        final Location bot = new Location(world, x1, 0, z1);
        Plot plot = bot.getOwnedPlotAbs();
        if (plot != null) {
            return plot;
        }
        final Location top = new Location(world, x2, 0, z2);
        plot = top.getOwnedPlotAbs();
        if (plot != null) {
            return plot;
        }
        return null;
    }
    
    /**
     * Copy a region to a new location (in the same world)
     */
    public abstract boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone);
    
    /**
     * Assumptions:<br>
     *  - pos1 and pos2 are in the same plot<br>
     * It can be harmful to the world if parameters outside this scope are provided
     * @param pos1
     * @param pos2
     * @param whenDone
     * @return
     */
    public abstract boolean regenerateRegion(final Location pos1, final Location pos2, boolean ignoreAugment, final Runnable whenDone);
    
    public abstract void clearAllEntities(final Location pos1, final Location pos2);
    
    public abstract void swap(final Location bot1, final Location top1, final Location bot2, final Location top2, Runnable whenDone);
}
