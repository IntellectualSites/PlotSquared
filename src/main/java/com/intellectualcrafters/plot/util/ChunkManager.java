package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;

public abstract class ChunkManager {
    
    public static ChunkManager manager = null;
    public static RegionWrapper CURRENT_PLOT_CLEAR = null;
    public static boolean FORCE_PASTE = false;
    
    public static HashMap<PlotLoc, HashMap<Short, Short>> GENERATE_BLOCKS = new HashMap<>();
    public static HashMap<PlotLoc, HashMap<Short, Byte>> GENERATE_DATA = new HashMap<>();

    public static ChunkLoc getChunkChunk(final Location loc) {
        final int x = loc.getX() >> 9;
        final int z = loc.getZ() >> 9;
        return new ChunkLoc(x, z);
    }
    
    /**
     * The int[] will be in the form: [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge] and will represent the bottom and top parts of the chunk
     * @param pos1
     * @param pos2
     * @param task
     * @param whenDone
     */
    public static void chunkTask(Location pos1, Location pos2, final RunnableVal<int[]> task, final Runnable whenDone, final int allocate) {
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
                long start = System.currentTimeMillis();
                while (chunks.size() > 0 && System.currentTimeMillis() - start < allocate) {
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
                if (chunks.size() != 0) {
                    TaskManager.runTaskLater(this, 1);
                }
                else {
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }
    
    public abstract void setChunk(ChunkWrapper loc, PlotBlock[][] result);
    
    public abstract int[] countEntities(Plot plot);

    public abstract boolean loadChunk(String world, ChunkLoc loc, boolean force);
    
    public abstract boolean unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe);

    public abstract List<ChunkLoc> getChunkChunks(String world);

    public abstract void regenerateChunk(String world, ChunkLoc loc);

    public abstract void deleteRegionFile(final String world, final ChunkLoc loc);
    
    public abstract void deleteRegionFiles(final String world, final List<ChunkLoc> chunks);

    public abstract Plot hasPlot(String world, ChunkLoc chunk);

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
    public abstract boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone);

    public abstract void clearAllEntities(final Plot plot);
    
    public abstract void swap(String world, PlotId id, PlotId plotid);

    public abstract void swap(String worldname, Location bot1, Location top1, Location bot2, Location top2);
}
