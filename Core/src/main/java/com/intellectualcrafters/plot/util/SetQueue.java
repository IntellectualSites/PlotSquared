package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class SetQueue {

    public static final SetQueue IMP = new SetQueue();
    private final AtomicInteger time_waiting = new AtomicInteger(2);
    private final AtomicInteger time_current = new AtomicInteger(0);
    private final ArrayDeque<Runnable> runnables = new ArrayDeque<>();
    public PlotQueue<?> queue;
    private long last;
    private long last2;

    public SetQueue() {
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                long free = 50 + Math.min(50 + SetQueue.this.last - (SetQueue.this.last = System.currentTimeMillis()),
                        SetQueue.this.last2 - System.currentTimeMillis());
                SetQueue.this.time_current.incrementAndGet();
                do {
                    if (isWaiting()) {
                        return;
                    }
                    PlotChunk<?> current = SetQueue.this.queue.next();
                    if (current == null) {
                        SetQueue.this.time_waiting.set(Math.max(SetQueue.this.time_waiting.get(), SetQueue.this.time_current.get() - 2));
                        tasks();
                        return;
                    }
                } while ((SetQueue.this.last2 = System.currentTimeMillis()) - SetQueue.this.last < free);
                SetQueue.this.time_waiting.set(SetQueue.this.time_current.get() - 1);
            }
        }, 1);
    }

    public boolean forceChunkSet() {
        PlotChunk<?> set = this.queue.next();
        return set != null;
    }

    public boolean isWaiting() {
        return this.time_waiting.get() >= this.time_current.get();
    }

    public boolean isDone() {
        return (this.time_waiting.get() + 1) < this.time_current.get();
    }

    public void setWaiting() {
        this.time_waiting.set(this.time_current.get() + 1);
    }

    public boolean addTask(Runnable whenDone) {
        if (isDone()) {
            // Run
            tasks();
            if (whenDone != null) {
                whenDone.run();
            }
            return true;
        }
        if (whenDone != null) {
            this.runnables.add(whenDone);
        }
        return false;
    }

    public boolean tasks() {
        if (this.runnables.isEmpty()) {
            return false;
        }
        ArrayDeque<Runnable> tmp = this.runnables.clone();
        this.runnables.clear();
        for (Runnable runnable : tmp) {
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
    public boolean setBlock(String world, int x, int y, int z, short id, byte data) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return this.queue.setBlock(world, x, y, z, id, data);
    }

    /**
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param block
     * @return
     */
    public boolean setBlock(String world, int x, int y, int z, PlotBlock block) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return this.queue.setBlock(world, x, y, z, block.id, block.data);
    }

    /**
     * @param world The world
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param id
     * @return
     */
    public boolean setBlock(String world, int x, int y, int z, short id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return this.queue.setBlock(world, x, y, z, id, (byte) 0);
    }

    /**
     *
     * @param world The world
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param id
     * @return
     */
    public boolean setBlock(String world, int x, int y, int z, int id) {
        if (y > 255 || y < 0) {
            return false;
        }
        SetQueue.IMP.setWaiting();
        return this.queue.setBlock(world, x, y, z, (short) id, (byte) 0);
    }

    public void regenerateChunk(String world, ChunkLoc loc) {
        queue.regenerateChunk(world, loc);
    }

    public class ChunkWrapper {

        public final int x;
        public final int z;
        public final String world;

        public ChunkWrapper(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            return (this.x << 16) | (this.z & 0xFFFF);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.hashCode() != obj.hashCode()) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChunkWrapper other = (ChunkWrapper) obj;
            return (this.x == other.x) && (this.z == other.z) && StringMan.isEqual(this.world, other.world);
        }

        @Override
        public String toString() {
            return this.world + ":" + this.x + "," + this.z;
        }
    }
}
