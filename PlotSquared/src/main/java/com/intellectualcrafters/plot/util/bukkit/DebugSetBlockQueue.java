package com.intellectualcrafters.plot.util.bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.plot.util.TaskManager;

public class DebugSetBlockQueue {
    
    private volatile static HashMap<ChunkWrapper, short[][]> blocks;
    private volatile static int allocate = 50;
    private volatile static boolean running = false;
    private volatile static boolean locked = false;
    private volatile static HashSet<Runnable> runnables;
    
    public synchronized static void allocate(int t) {
        allocate = t;
    }
    
    public synchronized static void addNotify(Runnable whenDone) {
        if (runnables == null) {
            TaskManager.runTask(whenDone);
        }
        else {
            runnables.add(whenDone);
        }
    }
    
    public synchronized static void init() {
        if (blocks == null) {
            blocks = new HashMap<>();
            runnables = new HashSet<>();
        }
        if (!running) {
            TaskManager.index.increment();
            final int current = TaskManager.index.intValue();
            int task = TaskManager.runTaskRepeat(new Runnable() {
                @Override
                public void run() {
                    if (locked) {
                        return;
                    }
                    if (blocks.size() == 0) {
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(current));
                        for (Runnable runnable : runnables) {
                            TaskManager.runTask(runnable);
                        }
                        runnables = null;
                        blocks = null;
                        running = false;
                        return;
                    }
                    long start = System.currentTimeMillis() + allocate;
                    Iterator<Entry<ChunkWrapper, short[][]>> i = blocks.entrySet().iterator();
                    while (System.currentTimeMillis() < start && i.hasNext()) {
                        if (locked) {
                            return;
                        }
                        Entry<ChunkWrapper, short[][]> n = i.next();
                        i.remove();
                        ChunkWrapper chunk = n.getKey();
                        int X = chunk.x << 4;
                        int Z = chunk.z << 4;
                        short[][] blocks = n.getValue();
                        World world = chunk.world;
                        for (int j = 0; j < blocks.length; j++) {
                            short[] blocksj = blocks[j];
                            if (blocksj != null) {
                                for (int k = 0; k < blocksj.length; k++) {
                                    short id = blocksj[k];
                                    if (id != 0) {
                                        final int y = (j << 4) + (k >> 8);
                                        final int a = (k - ((y & 0xF) << 8));
                                        final int z = (a >> 4);
                                        final int x = a - (z << 4);
                                        BukkitSetBlockManager.setBlockManager.set(world, X + x, y, Z + z, id, (byte) 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }, 5);
            TaskManager.tasks.put(current, task);
            running = true;
        }
    }
     
    public static void setBlock(final World world, int x, final int y, int z, final short blkid) {
        locked = true;
        if (!running) {
            init();
        }
        int X = x >> 4;
        int Z = z >> 4;
        x -= X << 4;
        z -= Z << 4;
        
        ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
        short[][] result = blocks.get(wrap);
        if (!blocks.containsKey(wrap)) {
            result = new short[16][];
            blocks.put(wrap, result);
        }
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
        locked = false;
    }
    
    public static class ChunkWrapper {
        final int x; 
        final int z;
        final World world;
        
        public ChunkWrapper(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public int hashCode() {
            int result;
            if (this.x >= 0) {
                if (this.z >= 0) {
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * this.z) + this.z + (this.z * this.z);
                } else {
                    final int y1 = -this.z;
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * y1) + y1 + (y1 * y1) + 1;
                }
            } else {
                final int x1 = -this.x;
                if (this.z >= 0) {
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * this.z) + this.z + (this.z * this.z));
                } else {
                    final int y1 = -this.z;
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
                }
            }
            result = result * 31 + world.hashCode();
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChunkWrapper other = (ChunkWrapper) obj;
            return ((this.x == other.x) && (this.z == other.z) && (this.world.equals(other.world)));
        }
    }
}
