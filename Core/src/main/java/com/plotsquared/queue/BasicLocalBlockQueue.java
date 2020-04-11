package com.plotsquared.queue;

import com.plotsquared.util.tasks.RunnableVal;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.MathMan;
import com.plotsquared.util.tasks.TaskManager;
import com.plotsquared.util.PatternUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;

public abstract class BasicLocalBlockQueue extends LocalBlockQueue {

    private final String world;
    private final ConcurrentHashMap<Long, LocalChunk> blockChunks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<LocalChunk> chunks = new ConcurrentLinkedDeque<>();
    private long modified;
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private boolean setbiome = false;

    public BasicLocalBlockQueue(String world) {
        super(world);
        this.world = world;
        this.modified = System.currentTimeMillis();
    }

    public abstract LocalChunk getLocalChunk(int x, int z);

    @Override public abstract BlockState getBlock(int x, int y, int z);

    public abstract void setComponents(LocalChunk lc)
        throws ExecutionException, InterruptedException;

    @Override public final String getWorld() {
        return world;
    }

    @Override public final boolean next() {
        lastX = Integer.MIN_VALUE;
        lastZ = Integer.MIN_VALUE;
        try {
            if (this.blockChunks.size() == 0) {
                return false;
            }
            synchronized (blockChunks) {
                LocalChunk chunk = chunks.poll();
                if (chunk != null) {
                    blockChunks.remove(chunk.longHash());
                    return this.execute(chunk);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public final boolean execute(@NotNull LocalChunk lc)
        throws ExecutionException, InterruptedException {
        this.setComponents(lc);
        return true;
    }

    @Override public void startSet(boolean parallel) {
        // Do nothing
    }

    @Override public void endSet(boolean parallel) {
        // Do nothing
    }

    @Override public final int size() {
        return chunks.size();
    }

    @Override public final long getModified() {
        return modified;
    }

    @Override public final void setModified(long modified) {
        this.modified = modified;
    }

    @Override public boolean setBlock(int x, int y, int z, @NotNull Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(pattern, x, y, z));
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        int cx = x >> 4;
        int cz = z >> 4;
        if (cx != lastX || cz != lastZ) {
            lastX = cx;
            lastZ = cz;
            long pair = (long) (cx) << 32 | (cz) & 0xFFFFFFFFL;
            lastWrappedChunk = this.blockChunks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = this.getLocalChunk(x >> 4, z >> 4);
                lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
                LocalChunk previous = this.blockChunks.put(pair, lastWrappedChunk);
                if (previous == null) {
                    return chunks.add(lastWrappedChunk);
                }
                this.blockChunks.put(pair, previous);
                lastWrappedChunk = previous;
            }
        }
        lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
        return true;
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        // Trying to mix BlockState and BaseBlock leads to all kinds of issues.
        // Since BaseBlock has more features than BlockState, simply convert
        // all BlockStates to BaseBlocks
        return setBlock(x, y, z, id.toBaseBlock());
    }

    @Override public final boolean setBiome(int x, int z, BiomeType biomeType) {
        long pair = (long) (x >> 4) << 32 | (z >> 4) & 0xFFFFFFFFL;
        LocalChunk result = this.blockChunks.get(pair);
        if (result == null) {
            result = this.getLocalChunk(x >> 4, z >> 4);
            LocalChunk previous = this.blockChunks.put(pair, result);
            if (previous != null) {
                this.blockChunks.put(pair, previous);
                result = previous;
            } else {
                chunks.add(result);
            }
        }
        result.setBiome(x & 15, z & 15, biomeType);
        setbiome = true;
        return true;
    }

    @Override public final boolean setBiome() {
        return setbiome;
    }

    public final void setChunk(LocalChunk chunk) {
        LocalChunk previous = this.blockChunks.put(chunk.longHash(), chunk);
        if (previous != null) {
            chunks.remove(previous);
        }
        chunks.add(chunk);
    }

    @Override public void flush() {
        GlobalBlockQueue.IMP.dequeue(this);
        TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override public void run(Object value) {
                while (next()) {
                }
            }
        });
    }


    public abstract class LocalChunk {
        public final BasicLocalBlockQueue parent;
        public final int z;
        public final int x;

        public BaseBlock[][] baseblocks;
        public BiomeType[][] biomes;

        public LocalChunk(BasicLocalBlockQueue parent, int x, int z) {
            this.parent = parent;
            this.x = x;
            this.z = z;
        }

        /**
         * Get the parent queue this chunk belongs to
         *
         * @return
         */
        public BasicLocalBlockQueue getParent() {
            return parent;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public abstract void setBlock(final int x, final int y, final int z, final BaseBlock block);

        public void setBiome(int x, int z, BiomeType biomeType) {
            if (this.biomes == null) {
                this.biomes = new BiomeType[16][];
            }
            BiomeType[] index = this.biomes[x];
            if (index == null) {
                index = this.biomes[x] = new BiomeType[16];
            }
            index[z] = biomeType;
        }

        public long longHash() {
            return MathMan.pairInt(x, z);
        }

        @Override public int hashCode() {
            return MathMan.pair((short) x, (short) z);
        }
    }


    public class BasicLocalChunk extends LocalChunk {

        public BasicLocalChunk(BasicLocalBlockQueue parent, int x, int z) {
            super(parent, x, z);
            baseblocks = new BaseBlock[16][];
        }

        @Override public void setBlock(int x, int y, int z, BaseBlock block) {
            this.setInternal(x, y, z, block);
        }

        private void setInternal(final int x, final int y, final int z, final BaseBlock baseBlock) {
            final int i = MainUtil.CACHE_I[y][x][z];
            final int j = MainUtil.CACHE_J[y][x][z];
            BaseBlock[] array = baseblocks[i];
            if (array == null) {
                array = (baseblocks[i] = new BaseBlock[4096]);
            }
            array[j] = baseBlock;
        }
    }
}
