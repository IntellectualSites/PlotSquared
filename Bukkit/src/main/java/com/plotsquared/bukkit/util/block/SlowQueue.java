package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.PlotQueue;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class SlowQueue implements PlotQueue<Chunk> {

    private final ConcurrentHashMap<ChunkWrapper, PlotChunk<Chunk>> blocks = new ConcurrentHashMap<>();

    public SlowQueue() {
        MainUtil.initCache();
    }

    @Override
    public boolean setBlock(String world, int x, int y, int z, short id, byte data) {
        if (y > 255 || y < 0) {
            return false;
        }
        ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(world, x >> 4, z >> 4);
        x = x & 15;
        z = z & 15;
        PlotChunk<Chunk> result = this.blocks.get(wrap);
        if (result == null) {
            result = getChunk(wrap);
            result.setBlock(x, y, z, id, data);
            PlotChunk<Chunk> previous = this.blocks.put(wrap, result);
            if (previous == null) {
                return true;
            }
            this.blocks.put(wrap, previous);
            result = previous;
        }
        result.setBlock(x, y, z, id, data);
        return true;
    }

    @Override
    public void setChunk(PlotChunk<Chunk> chunk) {
        this.blocks.put(chunk.getChunkWrapper(), chunk);
    }

    @Override
    public PlotChunk<Chunk> next() {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            throw new IllegalStateException("Must be called from main thread!");
        }
        try {
            if (this.blocks.isEmpty()) {
                return null;
            }
            Iterator<Entry<ChunkWrapper, PlotChunk<Chunk>>> iterator = this.blocks.entrySet().iterator();
            PlotChunk<Chunk> toReturn = iterator.next().getValue();
            if (SetQueue.IMP.isWaiting()) {
                return null;
            }
            iterator.remove();
            execute(toReturn);
            fixLighting(toReturn, true);
            return toReturn;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PlotChunk<Chunk> next(ChunkWrapper wrap, boolean fixLighting) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            throw new IllegalStateException("Must be called from main thread!");
        }
        try {
            if (this.blocks.isEmpty()) {
                return null;
            }
            PlotChunk<Chunk> toReturn = this.blocks.remove(wrap);
            if (toReturn == null) {
                return null;
            }
            execute(toReturn);
            fixLighting(toReturn, fixLighting);
            return toReturn;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void clear() {
        this.blocks.clear();
    }

    @Override
    public void regenerateChunk(String world, ChunkLoc loc) {
        BukkitUtil.getWorld(world).regenerateChunk(loc.x, loc.z);
    }

    /**
     * This should be overridden by any specialized queues.
     * @param plotChunk
     */
    public void execute(PlotChunk<Chunk> plotChunk) {
        SlowChunk sc = (SlowChunk) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        chunk.load(true);
        for (int i = 0; i < sc.result.length; i++) {
            PlotBlock[] result2 = sc.result[i];
            if (result2 == null) {
                continue;
            }
            for (int j = 0; j < 4096; j++) {
                int x = MainUtil.x_loc[i][j];
                int y = MainUtil.y_loc[i][j];
                int z = MainUtil.z_loc[i][j];
                Block block = chunk.getBlock(x, y, z);
                PlotBlock newBlock = result2[j];
                if (newBlock == null) {
                    continue;
                }
                switch (newBlock.id) {
                    case -1:
                        if (block.getData() == newBlock.data) {
                            continue;
                        }
                        block.setData(newBlock.data);
                        continue;
                    case 0:
                    case 2:
                    case 4:
                    case 13:
                    case 14:
                    case 15:
                    case 20:
                    case 21:
                    case 22:
                    case 25:
                    case 30:
                    case 32:
                    case 37:
                    case 39:
                    case 40:
                    case 41:
                    case 42:
                    case 45:
                    case 46:
                    case 47:
                    case 48:
                    case 49:
                    case 51:
                    case 52:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                    case 58:
                    case 60:
                    case 61:
                    case 62:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 73:
                    case 74:
                    case 78:
                    case 79:
                    case 80:
                    case 81:
                    case 82:
                    case 83:
                    case 84:
                    case 85:
                    case 87:
                    case 88:
                    case 101:
                    case 102:
                    case 103:
                    case 110:
                    case 112:
                    case 113:
                    case 117:
                    case 121:
                    case 122:
                    case 123:
                    case 124:
                    case 129:
                    case 133:
                    case 138:
                    case 137:
                    case 140:
                    case 165:
                    case 166:
                    case 169:
                    case 170:
                    case 172:
                    case 173:
                    case 174:
                    case 176:
                    case 177:
                    case 181:
                    case 182:
                    case 188:
                    case 189:
                    case 190:
                    case 191:
                    case 192:
                        if (block.getTypeId() == newBlock.id) {
                            continue;
                        }
                        block.setTypeId(newBlock.id, false);
                        continue;
                    default:
                        if (block.getTypeId() == newBlock.id && block.getData() == newBlock.data) {
                            continue;
                        }
                        if (newBlock.data == 0) {
                            block.setTypeId(newBlock.id, false);
                        } else {
                            block.setTypeIdAndData(newBlock.id, newBlock.data, false);
                        }
                        continue;
                }
            }
        }
        int[][] biomes = sc.biomes;
        Biome[] values = Biome.values();
        if (biomes != null) {
            for (int x = 0; x < 16; x++) {
                int[] array = biomes[x];
                if (array == null) {
                    continue;
                }
                for (int z = 0; z < 16; z++) {
                    int biome = array[z];
                    if (biome == 0) {
                        continue;
                    }
                    chunk.getBlock(x, 0, z).setBiome(values[biome]);
                }
            }
        }
    }

    /**
     * This should be overridden by any specialized queues.
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new SlowChunk(wrap);
    }

    /**
     * This should be overridden by any specialized queues.
     * @param fixAll
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> chunk, boolean fixAll) {
        // Do nothing
        return true;
    }

    /**
     * This should be overridden by any specialized queues.
     * @param locations
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locations) {
        // Do nothing
    }
}
