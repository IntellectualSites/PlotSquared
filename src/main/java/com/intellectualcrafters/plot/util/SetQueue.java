package com.intellectualcrafters.plot.util;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellectualcrafters.plot.object.PlotBlock;

public class SetQueue {
    
    public static final SetQueue IMP = new SetQueue();
    
    public PlotQueue<?> queue;
    
    private final AtomicInteger time_waiting = new AtomicInteger(2);
    private final AtomicInteger time_current = new AtomicInteger(0);
    private final ArrayDeque<Runnable> runnables = new ArrayDeque<>();
    private long last;
    private long last2;

    public SetQueue() {
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                long free = 50 + Math.min(50 + last - (last = System.currentTimeMillis()), last2 - System.currentTimeMillis());
                time_current.incrementAndGet();
                do {
                    if (isWaiting()) {
                        return;
                    }
                    final PlotChunk<?> current = queue.next();
                    if (current == null) {
                        time_waiting.set(Math.max(time_waiting.get(), time_current.get() - 2));
                        tasks();
                        return;
                    }
                } while ((last2 = System.currentTimeMillis()) - last < free);
                time_waiting.set(time_current.get() - 1);
            }
        }, 1);
    }
    
    public boolean forceChunkSet() {
        final PlotChunk<?> set = queue.next();
        return set != null;
    }
    
    public boolean isWaiting() {
        return time_waiting.get() >= time_current.get();
    }
    
    public boolean isDone() {
        return (time_waiting.get() + 1) < time_current.get();
    }
    
    public void setWaiting() {
        time_waiting.set(time_current.get() + 1);
    }
    
    public boolean addTask(final Runnable whenDone) {
        if (isDone()) {
            // Run
            tasks();
            if (whenDone != null) {
                whenDone.run();
            }
            return true;
        }
        if (whenDone != null) {
            runnables.add(whenDone);
        }
        return false;
    }
    
    public boolean tasks() {
        if (runnables.size() == 0) {
            return false;
        }
        final ArrayDeque<Runnable> tmp = runnables.clone();
        runnables.clear();
        for (final Runnable runnable : tmp) {
            runnable.run();
        }
        return true;
    }
    
    /**
     * @param world
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     * @return
     */
    public boolean setBlock(final String world, final int x, final int y, final int z, final short id, final byte data) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return queue.setBlock(world, x, y, z, id, data);
    }
    
    /**
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     * @return
     */
    public boolean setBlock(final String world, final int x, final int y, final int z, PlotBlock block) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return queue.setBlock(world, x, y, z, block.id, block.data);
    }
    
    /**
     * @param world
     * @param x
     * @param y
     * @param z
     * @param id
     * @return
     */
    public boolean setBlock(final String world, final int x, final int y, final int z, final short id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return queue.setBlock(world, x, y, z, id, (byte) 0);
    }
    
    /**
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @param id
     * @return
     */
    public boolean setBlock(final String world, final int x, final int y, final int z, final int id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return queue.setBlock(world, x, y, z, (short) id, (byte) 0);
    }
    
    public class ChunkWrapper {
        public final int x;
        public final int z;
        public final String world;
        
        public ChunkWrapper(final String world, final int x, final int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public int hashCode() {
            return (x << 16) | (z & 0xFFFF);
        }
        
        @Override
        public boolean equals(final Object obj) {
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
            return ((x == other.x) && (z == other.z) && (StringMan.isEqual(world, other.world)));
        }
        
        @Override
        public String toString() {
            return world + ":" + x + "," + z;
        }
    }
}
