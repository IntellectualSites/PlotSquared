package com.plotsquared.bukkit.util.block;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.SendChunk;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class FastQueue_1_7 extends SlowQueue {

    private final RefClass classBlock = getRefClass("{nms}.Block");
    private final RefClass classChunk = getRefClass("{nms}.Chunk");
    private final RefClass classWorld = getRefClass("{nms}.World");
    private final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private final RefMethod methodGetHandle;
    private final RefMethod methodGetChunkAt;
    private final RefMethod methodA;
    private final RefMethod methodGetById;
    private final RefMethod methodInitLighting;

    private final SendChunk chunksender;

    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();

    public FastQueue_1_7() throws RuntimeException {
        this.methodGetHandle = this.classCraftWorld.getMethod("getHandle");
        this.methodGetChunkAt = this.classWorld.getMethod("getChunkAt", int.class, int.class);
        this.methodA = this.classChunk.getMethod("a", int.class, int.class, int.class, this.classBlock, int.class);
        this.methodGetById = this.classBlock.getMethod("getById", int.class);
        this.methodInitLighting = this.classChunk.getMethod("initLighting");
        this.chunksender = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (FastQueue_1_7.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<>();
                Iterator<Entry<ChunkWrapper, Chunk>> i = FastQueue_1_7.this.toUpdate.entrySet().iterator();
                while (i.hasNext() && (count < 128)) {
                    chunks.add(i.next().getValue());
                    i.remove();
                    count++;
                }
                if (count == 0) {
                    return;
                }
                update(chunks);
            }
        }, 1);
        MainUtil.initCache();
    }

    public void update(Collection<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            for (Chunk chunk : chunks) {
                chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                chunk.unload(true, false);
                chunk.load();
            }
            return;
        }
        try {
            this.chunksender.sendChunk(chunks);
        } catch (Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    /**
     * This should be overridden by any specialized queues
     * @param plotChunk
     */
    @Override
    public void execute(PlotChunk<Chunk> plotChunk) {
        SlowChunk sc = (SlowChunk) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        ChunkWrapper wrapper = plotChunk.getChunkWrapper();
        if (!this.toUpdate.containsKey(wrapper)) {
            this.toUpdate.put(wrapper, chunk);
        }
        chunk.load(true);
        World world = chunk.getWorld();
        Object w = this.methodGetHandle.of(world).call();
        Object c = this.methodGetChunkAt.of(w).call(wrapper.x, wrapper.z);
        for (int i = 0; i < sc.result.length; i++) {
            PlotBlock[] result2 = sc.result[i];
            if (result2 == null) {
                continue;
            }
            for (int j = 0; j < 4096; j++) {
                int x = MainUtil.x_loc[i][j];
                int y = MainUtil.y_loc[i][j];
                int z = MainUtil.z_loc[i][j];
                PlotBlock newBlock = result2[j];
                if (newBlock.id == -1) {
                    chunk.getBlock(x, y, z).setData(newBlock.data, false);
                    continue;
                }
                Object block = this.methodGetById.call(newBlock.id);
                this.methodA.of(c).call(x, y, z, block, newBlock.data);
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
     * This should be overridden by any specialized queues
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new SlowChunk(wrap);
    }

    /**
     * This should be overridden by any specialized queues
     * @param chunk
     * @param fixAll
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> chunk, boolean fixAll) {
        Object c = this.methodGetHandle.of(chunk.getChunk()).call();
        this.methodInitLighting.of(c).call();
        return true;
    }

    /**
     * This should be overridden by any specialized queues
     * @param world
     * @param locations
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locations) {
        World worldObj = BukkitUtil.getWorld(world);
        for (ChunkLoc loc : locations) {
            ChunkWrapper wrapper = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            this.toUpdate.remove(wrapper);
        }
        this.chunksender.sendChunk(world, locations);
    }
}
