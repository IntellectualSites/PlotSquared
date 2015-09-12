package com.intellectualcrafters.plot.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;

public class SetBlockQueue
{

    private volatile static HashMap<ChunkWrapper, PlotBlock[][]> blocks;
    private volatile static int allocate = 25;
    private volatile static boolean running = false;
    private volatile static boolean locked = false;
    private volatile static ArrayDeque<Runnable> runnables;
    private volatile static boolean slow = false;
    private static long last;
    private static int lastInt = 0;
    private static PlotBlock lastBlock = new PlotBlock((short) 0, (byte) 0);

    public synchronized static void allocate(final int t)
    {
        allocate = t;
    }

    public static int getAllocate()
    {
        return allocate;
    }

    public static void setSlow(final boolean value)
    {
        slow = value;
    }

    public synchronized static boolean addNotify(final Runnable whenDone)
    {
        if (runnables == null)
        {
            if ((blocks == null) || (blocks.size() == 0))
            {
                if (whenDone != null)
                {
                    whenDone.run();
                }
                slow = false;
                locked = false;
                return true;
            }
            runnables = new ArrayDeque<>();
        }
        if (whenDone != null)
        {
            init();
            runnables.add(whenDone);
        }
        if ((blocks == null) || (blocks.size() == 0) || !blocks.entrySet().iterator().hasNext())
        {
            final ArrayDeque<Runnable> tasks = runnables;
            lastInt = -1;
            lastBlock = null;
            runnables = null;
            running = false;
            blocks = null;
            slow = false;
            if (tasks != null)
            {
                for (final Runnable runnable : tasks)
                {
                    runnable.run();
                }
            }
        }
        return false;
    }

    public synchronized static void init()
    {
        if (blocks == null)
        {
            if (MainUtil.x_loc == null)
            {
                MainUtil.initCache();
            }
            blocks = new HashMap<>();
            runnables = new ArrayDeque<>();
        }
        if (!running)
        {
            TaskManager.index.incrementAndGet();
            final int current = TaskManager.index.intValue();
            final int task = TaskManager.runTaskRepeat(new Runnable()
            {
                @Override
                public void run()
                {
                    if (locked) {
                        return;
                    }
                    if ((blocks == null) || (blocks.size() == 0))
                    {
                        PS.get().TASK.cancelTask(TaskManager.tasks.get(current));
                        final ArrayDeque<Runnable> tasks = runnables;
                        lastInt = -1;
                        lastBlock = null;
                        runnables = null;
                        running = false;
                        blocks = null;
                        slow = false;
                        if (tasks != null)
                        {
                            for (final Runnable runnable : tasks)
                            {
                                runnable.run();
                            }
                        }
                        return;
                    }
                    final long newLast = System.currentTimeMillis();
                    last = Math.max(newLast - 50, last);
                    while ((blocks.size() > 0) && ((System.currentTimeMillis() - last) < (50 + allocate)))
                    {
                        if (locked) {
                            return;
                        }
                        final Iterator<Entry<ChunkWrapper, PlotBlock[][]>> iter = blocks.entrySet().iterator();
                        if (!iter.hasNext())
                        {
                            PS.get().TASK.cancelTask(TaskManager.tasks.get(current));
                            final ArrayDeque<Runnable> tasks = runnables;
                            lastInt = -1;
                            lastBlock = null;
                            runnables = null;
                            running = false;
                            blocks = null;
                            slow = false;
                            if (tasks != null)
                            {
                                for (final Runnable runnable : tasks)
                                {
                                    runnable.run();
                                }
                            }
                            return;
                        }
                        final Entry<ChunkWrapper, PlotBlock[][]> n = iter.next();
                        final ChunkWrapper chunk = n.getKey();
                        final PlotBlock[][] blocks = n.getValue();
                        final int X = chunk.x << 4;
                        final int Z = chunk.z << 4;
                        final String world = chunk.world;
                        if (slow)
                        {
                            boolean once = false;
                            for (int j = 0; j < blocks.length; j++)
                            {
                                final PlotBlock[] blocksj = blocks[j];
                                if (blocksj != null)
                                {
                                    final long start = System.currentTimeMillis();
                                    for (int k = 0; k < blocksj.length; k++)
                                    {
                                        if (once && ((System.currentTimeMillis() - start) > allocate))
                                        {
                                            SetBlockQueue.blocks.put(n.getKey(), blocks);
                                            return;
                                        }
                                        final PlotBlock block = blocksj[k];
                                        if (block != null)
                                        {
                                            final int x = MainUtil.x_loc[j][k];
                                            final int y = MainUtil.y_loc[j][k];
                                            final int z = MainUtil.z_loc[j][k];
                                            BlockManager.manager.functionSetBlock(world, X + x, y, Z + z, block.id, block.data);
                                            blocks[j][k] = null;
                                            once = true;
                                        }
                                    }
                                }
                            }
                            SetBlockQueue.blocks.remove(n.getKey());
                            return;
                        }
                        SetBlockQueue.blocks.remove(n.getKey());
                        for (int j = 0; j < blocks.length; j++)
                        {
                            final PlotBlock[] blocksj = blocks[j];
                            if (blocksj != null)
                            {
                                for (int k = 0; k < blocksj.length; k++)
                                {
                                    final PlotBlock block = blocksj[k];
                                    if (block != null)
                                    {
                                        final int x = MainUtil.x_loc[j][k];
                                        final int y = MainUtil.y_loc[j][k];
                                        final int z = MainUtil.z_loc[j][k];
                                        BlockManager.manager.functionSetBlock(world, X + x, y, Z + z, block.id, block.data);
                                    }
                                }
                            }
                        }
                    }
                }
            }, 1);
            TaskManager.tasks.put(current, task);
            running = true;
        }
    }

    public static void setChunk(final String world, final ChunkLoc loc, final PlotBlock[][] result)
    {
        locked = true;
        if (!running)
        {
            init();
        }
        final ChunkWrapper wrap = new ChunkWrapper(world, loc.x, loc.z);
        blocks.put(wrap, result);
        locked = false;
    }

    public static void setBlock(final String world, int x, final int y, int z, final PlotBlock block)
    {
        locked = true;
        if (!running)
        {
            init();
        }
        final int X = x >> 4;
                                final int Z = z >> 4;
                            x -= X << 4;
                            z -= Z << 4;

                            final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                            PlotBlock[][] result;
                            result = blocks.get(wrap);
                            if (!blocks.containsKey(wrap))
                            {
                                result = new PlotBlock[16][];
                                blocks.put(wrap, result);
                            }
                            if ((y > 255) || (y < 0))
                            {
                                locked = false;
                                return;
                            }
                            if (result[y >> 4] == null)
                            {
                                result[y >> 4] = new PlotBlock[4096];
                            }
                            result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = block;
                            locked = false;
    }

    public static void setData(final String world, int x, final int y, int z, final byte data)
    {
        locked = true;
        if (!running)
        {
            init();
        }
        final int X = x >> 4;
                            final int Z = z >> 4;
                            x -= X << 4;
                            z -= Z << 4;
                            final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                            PlotBlock[][] result;
                            result = blocks.get(wrap);
                            if (result == null)
                            {
                                if (blocks == null)
                                {
                                    init();
                                }
                                result = new PlotBlock[16][];
                                blocks.put(wrap, result);
                            }
                            if ((y > 255) || (y < 0))
                            {
                                locked = false;
                                return;
                            }
                            if (result[y >> 4] == null)
                            {
                                result[y >> 4] = new PlotBlock[4096];
                            }
                            result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = new PlotBlock((short) -1, data);
                            locked = false;
    }

    public static void setBlock(final String world, int x, final int y, int z, final int id)
    {
        locked = true;
        if (!running)
        {
            init();
        }
        final int X = x >> 4;
                            final int Z = z >> 4;
                            x -= X << 4;
                            z -= Z << 4;
                            final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                            PlotBlock[][] result;
                            result = blocks.get(wrap);
                            if (result == null)
                            {
                                if (blocks == null)
                                {
                                    init();
                                }
                                result = new PlotBlock[16][];
                                blocks.put(wrap, result);
                            }
                            if ((y > 255) || (y < 0))
                            {
                                locked = false;
                                return;
                            }
                            if (result[y >> 4] == null)
                            {
                                result[y >> 4] = new PlotBlock[4096];
                            }
                            if (id == lastInt)
                            {
                                result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
                            }
                            else
                            {
                                lastInt = id;
                                lastBlock = new PlotBlock((short) id, (byte) 0);
                            }
                            result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
                            locked = false;
    }

    public static class ChunkWrapper
    {
        public final int x;
        public final int z;
        public final String world;

        public ChunkWrapper(final String world, final int x, final int z)
        {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode()
        {
            int result;
            if (x >= 0)
            {
                if (z >= 0)
                {
                    result = (x * x) + (3 * x) + (2 * x * z) + z + (z * z);
                }
                else
                {
                    final int y1 = -z;
                    result = (x * x) + (3 * x) + (2 * x * y1) + y1 + (y1 * y1) + 1;
                }
            }
            else
            {
                final int x1 = -x;
                if (z >= 0)
                {
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * z) + z + (z * z));
                }
                else
                {
                    final int y1 = -z;
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
                }
            }
            result = (result * 31) + world.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final ChunkWrapper other = (ChunkWrapper) obj;
            return ((x == other.x) && (z == other.z) && (world.equals(other.world)));
        }
    }
}
