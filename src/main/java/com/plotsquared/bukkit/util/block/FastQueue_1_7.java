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

    public final RefClass classBlock = getRefClass("{nms}.Block");
    public final RefClass classChunk = getRefClass("{nms}.Chunk");
    public final RefClass classWorld = getRefClass("{nms}.World");
    public final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    public final RefMethod methodGetHandle;
    public final RefMethod methodGetChunkAt;
    public final RefMethod methodA;
    public final RefMethod methodGetById;
    public final RefMethod methodInitLighting;

    public final SendChunk chunksender;

    public HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();

    public FastQueue_1_7() throws NoSuchMethodException {
        methodGetHandle = classCraftWorld.getMethod("getHandle");
        methodGetChunkAt = classWorld.getMethod("getChunkAt", int.class, int.class);
        methodA = classChunk.getMethod("a", int.class, int.class, int.class, classBlock, int.class);
        methodGetById = classBlock.getMethod("getById", int.class);
        methodInitLighting = classChunk.getMethod("initLighting");
        chunksender = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                final ArrayList<Chunk> chunks = new ArrayList<>();
                final Iterator<Entry<ChunkWrapper, Chunk>> i = toUpdate.entrySet().iterator();
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

    public void update(final Collection<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            for (final Chunk chunk : chunks) {
                chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                chunk.unload(true, false);
                chunk.load();
            }
            return;
        }
        try {
            chunksender.sendChunk(chunks);
        } catch (final Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    /**
     * This should be overriden by any specialized queues 
     * @param pc
     */
    @Override
    public void execute(PlotChunk<Chunk> pc) {
        SlowChunk sc = (SlowChunk) pc;
        Chunk chunk = pc.getChunk();
        ChunkWrapper wrapper = pc.getChunkWrapper();
        if (!toUpdate.containsKey(wrapper)) {
            toUpdate.put(wrapper, chunk);
        }
        chunk.load(true);
        World world = chunk.getWorld();
        final Object w = methodGetHandle.of(world).call();
        final Object c = methodGetChunkAt.of(w).call(wrapper.x, wrapper.z);
        for (int i = 0; i < sc.result.length; i++) {
            PlotBlock[] result2 = sc.result[i];
            if (result2 == null) {
                continue;
            }
            for (int j = 0; j < 4096; j++) {
                final int x = MainUtil.x_loc[i][j];
                final int y = MainUtil.y_loc[i][j];
                final int z = MainUtil.z_loc[i][j];
                PlotBlock newBlock = result2[j];
                if (newBlock.id == -1) {
                    chunk.getBlock(x, y, z).setData(newBlock.data, false);
                    continue;
                }
                final Object block = methodGetById.call(newBlock.id);
                methodA.of(c).call(x, y, z, block, newBlock.data);
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
     * This should be overriden by any specialized queues 
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new SlowChunk(wrap);
    }

    /**
     * This should be overriden by any specialized queues 
     * @param chunk
     * @param fixAll
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> chunk, boolean fixAll) {
        Object c = methodGetHandle.of(chunk.getChunk()).call();
        methodInitLighting.of(c).call();
        return true;
    }

    /**
     * This should be overriden by any specialized queues 
     * @param world
     * @param locs
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locs) {
        World worldObj = BukkitUtil.getWorld(world);
        for (ChunkLoc loc : locs) {
            ChunkWrapper wrapper = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            if (!toUpdate.containsKey(wrapper)) {
                toUpdate.put(wrapper, worldObj.getChunkAt(loc.x, loc.z));
            }
        }
    }
}
