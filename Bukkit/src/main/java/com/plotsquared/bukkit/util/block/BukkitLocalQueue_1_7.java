package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.SendChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public class BukkitLocalQueue_1_7 extends BukkitLocalQueue<PlotBlock[]> {

    private final ReflectionUtils.RefClass classBlock = getRefClass("{nms}.Block");
    private final ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
    private final ReflectionUtils.RefClass classWorld = getRefClass("{nms}.World");
    private final ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private final ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    private final ReflectionUtils.RefMethod methodGetHandle;
    private final ReflectionUtils.RefMethod methodGetHandleChunk;
    private final ReflectionUtils.RefMethod methodGetChunkAt;
    private final ReflectionUtils.RefMethod methodA;
    private final ReflectionUtils.RefMethod methodGetById;
    private final ReflectionUtils.RefMethod methodInitLighting;
    private final SendChunk sendChunk;

    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();

    public BukkitLocalQueue_1_7(String world) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        super(world);
        this.methodGetHandle = this.classCraftWorld.getMethod("getHandle");
        this.methodGetChunkAt = this.classWorld.getMethod("getChunkAt", int.class, int.class);
        this.methodA = this.classChunk.getMethod("a", int.class, int.class, int.class, this.classBlock, int.class);
        this.methodGetById = this.classBlock.getMethod("getById", int.class);
        this.methodGetHandleChunk = this.classCraftChunk.getMethod("getHandle");
        this.methodInitLighting = this.classChunk.getMethod("initLighting");
        this.sendChunk = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (BukkitLocalQueue_1_7.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<>();
                Iterator<Map.Entry<ChunkWrapper, Chunk>> i = BukkitLocalQueue_1_7.this.toUpdate.entrySet().iterator();
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
            this.sendChunk.sendChunk(chunks);
        } catch (Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    @Override
    public void fixChunkLighting(int x, int z) {
        Object c = this.methodGetHandleChunk.of(getChunk(x, z)).call();
        this.methodInitLighting.of(c).call();
    }

    @Override
    public void setBlocks(LocalChunk<PlotBlock[]> lc) {
        Chunk chunk = getChunk(lc.getX(), lc.getZ());
        chunk.load(true);
        World world = chunk.getWorld();
        ChunkWrapper wrapper = new ChunkWrapper(getWorld(), lc.getX(), lc.getZ());
        if (!this.toUpdate.containsKey(wrapper)) {
            this.toUpdate.put(wrapper, chunk);
        }
        Object w = this.methodGetHandle.of(world).call();
        Object c = this.methodGetChunkAt.of(w).call(lc.getX(), lc.getZ());
        for (int i = 0; i < lc.blocks.length; i++) {
            PlotBlock[] result2 = lc.blocks[i];
            if (result2 == null) {
                continue;
            }
            for (int j = 0; j < 4096; j++) {
                int x = MainUtil.x_loc[i][j];
                int y = MainUtil.y_loc[i][j];
                int z = MainUtil.z_loc[i][j];
                PlotBlock newBlock = result2[j];
                if (newBlock != null) {
                    if (newBlock.id == -1) {
                        chunk.getBlock(x, y, z).setData(newBlock.data, false);
                        continue;
                    }
                    Object block = this.methodGetById.call(newBlock.id);
                    this.methodA.of(c).call(x, y, z, block, newBlock.data);
                }
            }
        }
        fixChunkLighting(lc.getX(), lc.getZ());
    }

    @Override
    public void refreshChunk(int x, int z) {
        update(Collections.singletonList(Bukkit.getWorld(getWorld()).getChunkAt(x, z)));
    }
}
