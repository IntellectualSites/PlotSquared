package com.intellectualcrafters.plot.util.bukkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.util.TaskManager;

public class DebugSetBlockQueue {
    
    private HashMap<Chunk, short[][]> blocks;
    private int allocate = 100;
    
    public void allocate(int t) {
        this.allocate = t;
    }
    
    public DebugSetBlockQueue() {
        blocks = new HashMap<>();
        TaskManager.index.increment();
        final int current = TaskManager.index.intValue();
        int task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (blocks.size() == 0) {
                    blocks = null;
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(current));
                    return;
                }
                long start = System.currentTimeMillis() + allocate;
                Iterator<Entry<Chunk, short[][]>> i = blocks.entrySet().iterator();
                while (System.currentTimeMillis() < start && i.hasNext()) {
                    Entry<Chunk, short[][]> n = i.next();
                    i.remove();
                    Chunk chunk = n.getKey();
                    int X = chunk.getX() << 4;
                    int Z = chunk.getZ() << 4;
                    short[][] blocks = n.getValue();
                    World world = chunk.getWorld();
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
        }, 20);
        TaskManager.tasks.put(current, task);
    }
    
    
    
    public void setBlock(final World world, int x, final int y, int z, final short blkid) {
        int X = x >> 4;
        int Z = z >> 4;
        x -= X << 4;
        z -= Z << 4;
        Chunk chunk = world.getChunkAt(X, Z);
        short[][] result = blocks.get(chunk);
        if (!blocks.containsKey(chunk)) {
            result = new short[16][];
            blocks.put(chunk, result);
        }
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
}
