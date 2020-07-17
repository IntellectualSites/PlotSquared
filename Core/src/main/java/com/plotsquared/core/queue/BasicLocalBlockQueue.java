/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.queue;

import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import javax.annotation.Nonnull;
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

    private GlobalBlockQueue globalBlockQueue;

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

    public final boolean execute(@Nonnull LocalChunk lc)
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

    @Override public boolean setBlock(int x, int y, int z, @Nonnull Pattern pattern) {
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
        this.globalBlockQueue.dequeue(this);
        try {
            TaskManager.getImplementation().sync(() -> {
                while (next()) {
                }
                return null;
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
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
