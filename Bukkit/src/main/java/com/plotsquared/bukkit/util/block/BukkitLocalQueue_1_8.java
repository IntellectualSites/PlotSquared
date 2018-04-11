package com.plotsquared.bukkit.util.block;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.SendChunk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BukkitLocalQueue_1_8 extends BukkitLocalQueue<PlotBlock[]> {

    private final ReflectionUtils.RefMethod methodInitLighting;
    private final ReflectionUtils.RefClass classBlock = getRefClass("{nms}.Block");
    private final ReflectionUtils.RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
    private final ReflectionUtils.RefClass classIBlockData = getRefClass("{nms}.IBlockData");
    private final ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
    private final ReflectionUtils.RefClass classWorld = getRefClass("{nms}.World");
    private final ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final ReflectionUtils.RefMethod methodGetHandle;
    private final ReflectionUtils.RefMethod methodGetChunkAt;
    private final ReflectionUtils.RefMethod methodA;
    private final ReflectionUtils.RefMethod methodGetByCombinedId;
    private final ReflectionUtils.RefConstructor constructorBlockPosition;
    private final SendChunk sendChunk;
    private final RefMethod methodGetHandleChunk;

    public BukkitLocalQueue_1_8(String world) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        super(world);
        this.methodInitLighting = this.classChunk.getMethod("initLighting");
        this.constructorBlockPosition = this.classBlockPosition.getConstructor(int.class, int.class, int.class);
        this.methodGetByCombinedId = this.classBlock.getMethod("getByCombinedId", int.class);
        this.methodGetHandle = this.classCraftWorld.getMethod("getHandle");
        RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        this.methodGetChunkAt = this.classWorld.getMethod("getChunkAt", int.class, int.class);
        this.methodA = this.classChunk.getMethod("a", this.classBlockPosition, this.classIBlockData);
        this.sendChunk = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (BukkitLocalQueue_1_8.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<>();
                Iterator<Map.Entry<ChunkWrapper, Chunk>> i = BukkitLocalQueue_1_8.this.toUpdate.entrySet().iterator();
                while (i.hasNext() && count < 128) {
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
                if (newBlock == null) continue;

                if (newBlock.id == -1) {
                    chunk.getBlock(x, y, z).setData(newBlock.data, false);
                    continue;
                }
                // Start blockstate workaround //
                switch (newBlock.id) {
                    case 54:
                    case 130:
                    case 142:
                    case 132:
                    case 27:
                    case 137:
                    case 52:
                    case 154:
                    case 84:
                    case 25:
                    case 144:
                    case 138:
                    case 176:
                    case 177:
                    case 119:
                    case 63:
                    case 68:
                    case 323:
                    case 117:
                    case 116:
                    case 28:
                    case 66:
                    case 157:
                    case 61:
                    case 62:
                    case 140:
                    case 146:
                    case 149:
                    case 150:
                    case 158:
                    case 23:
                    case 123:
                    case 124:
                    case 29:
                    case 33:
                    case 151:
                    case 178:
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getData() == newBlock.data) {
                            if (block.getTypeId() != newBlock.id) {
                                block.setTypeId(newBlock.id, false);
                            }
                        } else {
                            if (block.getTypeId() == newBlock.id) {
                                block.setData(newBlock.data, false);
                            } else {
                                block.setTypeIdAndData(newBlock.id, newBlock.data, false);
                            }
                        }
                        continue;
                }

                // Start data value shortcut
                Block block = world.getBlockAt(x, y, z);
                int currentId = block.getTypeId();
                if (currentId == newBlock.id) {
                    switch (newBlock.id) {
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
                            continue;
                    }
                    if (block.getData() == newBlock.data) {
                        return;
                    }
                    block.setData(newBlock.data, false);
                    return;
                }
                // blockstate
                switch (currentId) {
                    case 54:
                    case 130:
                    case 132:
                    case 142:
                    case 27:
                    case 137:
                    case 52:
                    case 154:
                    case 84:
                    case 25:
                    case 144:
                    case 138:
                    case 176:
                    case 177:
                    case 63:
                    case 68:
                    case 323:
                    case 117:
                    case 119:
                    case 116:
                    case 28:
                    case 66:
                    case 157:
                    case 61:
                    case 62:
                    case 140:
                    case 146:
                    case 149:
                    case 150:
                    case 158:
                    case 23:
                    case 123:
                    case 124:
                    case 29:
                    case 33:
                    case 151:
                    case 178:
                        if (block.getData() == newBlock.data) {
                            block.setTypeId(newBlock.id, false);
                        } else {
                            block.setTypeIdAndData(newBlock.id, newBlock.data, false);
                        }
                        continue;
                }
                // End blockstate workaround //

                // check sign
                Object pos = null;
                try {
                    pos = this.constructorBlockPosition.create(x, y, z);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
                Object combined = this.methodGetByCombinedId.call(newBlock.id + (newBlock.data << 12));
                this.methodA.of(c).call(pos, combined);
            }
        }
        fixChunkLighting(lc.getX(), lc.getZ());
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
    public void refreshChunk(int x, int z) {
        update(Arrays.asList(Bukkit.getWorld(getWorld()).getChunkAt(x, z)));
    }
}
