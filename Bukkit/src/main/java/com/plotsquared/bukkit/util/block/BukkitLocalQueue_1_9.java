package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.block.BasicLocalBlockQueue;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public class BukkitLocalQueue_1_9 extends BukkitLocalQueue<char[]> {

    private final Object air;
    //    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final ReflectionUtils.RefMethod methodGetHandleChunk;
    private final ReflectionUtils.RefMethod methodInitLighting;
    private final ReflectionUtils.RefConstructor classBlockPositionConstructor;
    private final ReflectionUtils.RefConstructor classChunkSectionConstructor;
    private final ReflectionUtils.RefMethod methodW;
    private final ReflectionUtils.RefMethod methodAreNeighborsLoaded;
    private final ReflectionUtils.RefField fieldSections;
    private final ReflectionUtils.RefField fieldWorld;
    private final ReflectionUtils.RefMethod methodGetBlocks;
    private final ReflectionUtils.RefMethod methodGetType;
    private final ReflectionUtils.RefMethod methodSetType;
    private final ReflectionUtils.RefMethod methodGetCombinedId;
    private final ReflectionUtils.RefMethod methodGetByCombinedId;
    private final ReflectionUtils.RefMethod methodGetWorld;

    private final ReflectionUtils.RefField tileEntityListTick;

    public BukkitLocalQueue_1_9(String world) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        super(world);
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        ReflectionUtils.RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
        this.classBlockPositionConstructor = classBlockPosition.getConstructor(int.class, int.class, int.class);
        ReflectionUtils.RefClass classWorld = getRefClass("{nms}.World");
        this.tileEntityListTick = classWorld.getField("tileEntityListTick");
        this.methodGetWorld = classChunk.getMethod("getWorld");
        this.methodW = classWorld.getMethod("w", classBlockPosition.getRealClass());
        this.fieldSections = classChunk.getField("sections");
        this.fieldWorld = classChunk.getField("world");
        ReflectionUtils.RefClass classBlock = getRefClass("{nms}.Block");
        ReflectionUtils.RefClass classIBlockData = getRefClass("{nms}.IBlockData");
        this.methodGetCombinedId = classBlock.getMethod("getCombinedId", classIBlockData.getRealClass());
        this.methodGetByCombinedId = classBlock.getMethod("getByCombinedId", int.class);
        ReflectionUtils.RefClass classChunkSection = getRefClass("{nms}.ChunkSection");
        this.methodGetBlocks = classChunkSection.getMethod("getBlocks");
        this.methodGetType = classChunkSection.getMethod("getType", int.class, int.class, int.class);
        this.methodSetType = classChunkSection.getMethod("setType", int.class, int.class, int.class, classIBlockData.getRealClass());
        this.methodAreNeighborsLoaded = classChunk.getMethod("areNeighborsLoaded", int.class);
        this.classChunkSectionConstructor = classChunkSection.getConstructor(int.class, boolean.class, char[].class);
        this.air = this.methodGetByCombinedId.call(0);
        MainUtil.initCache();
    }

    @Override
    public LocalChunk<char[]> getLocalChunk(int x, int z) {
        return new CharLocalChunk_1_8_3(this, x, z);
    }

    public class CharLocalChunk_1_8_3 extends CharLocalChunk {
        public short[] count;
        public short[] air;
        public short[] relight;

        public CharLocalChunk_1_8_3(BasicLocalBlockQueue parent, int x, int z) {
            super(parent, x, z);
            this.count = new short[16];
            this.air = new short[16];
            this.relight = new short[16];
        }

        @Override
        public void setBlock(int x, int y, int z, int id, int data) {
            int i = MainUtil.CACHE_I[y][x][z];
            int j = MainUtil.CACHE_J[y][x][z];
            char[] vs = this.blocks[i];
            if (vs == null) {
                vs = this.blocks[i] = new char[4096];
                this.count[i]++;
            } else if (vs[j] == 0) {
                this.count[i]++;
            }
            switch (id) {
                case 0:
                    this.air[i]++;
                    vs[j] = (char) 1;
                    return;
                case 10:
                case 11:
                case 39:
                case 40:
                case 51:
                case 74:
                case 89:
                case 122:
                case 124:
                case 138:
                case 169:
                    this.relight[i]++;
                case 2:
                case 4:
                case 13:
                case 14:
                case 15:
                case 20:
                case 21:
                case 22:
                case 30:
                case 32:
                case 37:
                case 41:
                case 42:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 55:
                case 56:
                case 57:
                case 58:
                case 60:
                case 7:
                case 8:
                case 9:
                case 73:
                case 78:
                case 79:
                case 80:
                case 81:
                case 82:
                case 83:
                case 85:
                case 87:
                case 88:
                case 101:
                case 102:
                case 103:
                case 110:
                case 112:
                case 113:
                case 121:
                case 129:
                case 133:
                case 165:
                case 166:
                case 170:
                case 172:
                case 173:
                case 174:
                case 181:
                case 182:
                case 188:
                case 189:
                case 190:
                case 191:
                case 192:
                    vs[j] = (char) (id << 4);
                    return;
                case 130:
                case 76:
                case 62:
                    this.relight[i]++;
                case 54:
                case 146:
                case 61:
                case 65:
                case 68:
                case 50:
                    if (data < 2) {
                        data = 2;
                    }
                default:
                    vs[j] = (char) ((id << 4) + data);
                    return;
            }
        }

        public char[] getIdArray(int i) {
            return this.blocks[i];
        }

        public int getCount(int i) {
            return this.count[i];
        }

        public int getAir(int i) {
            return this.air[i];
        }

        public void setCount(int i, short value) {
            this.count[i] = value;
        }

        public int getRelight(int i) {
            return this.relight[i];
        }

        public int getTotalCount() {
            int total = 0;
            for (int i = 0; i < 16; i++) {
                total += this.count[i];
            }
            return total;
        }

        public int getTotalRelight() {
            if (getTotalCount() == 0) {
                Arrays.fill(this.count, (short) 1);
                Arrays.fill(this.relight, Short.MAX_VALUE);
                return Short.MAX_VALUE;
            }
            int total = 0;
            for (int i = 0; i < 16; i++) {
                total += this.relight[i];
            }
            return total;
        }
    }

    @Override public void setBlocks(LocalChunk<char[]> lc) {
        CharLocalChunk_1_8_3 fs = (CharLocalChunk_1_8_3) lc;
        Chunk chunk = getChunk(lc.getX(), lc.getZ());
        chunk.load(true);
        World world = chunk.getWorld();
        try {
            boolean flag = world.getEnvironment() == World.Environment.NORMAL;

            // Sections
            Method getHandle = chunk.getClass().getDeclaredMethod("getHandle");
            Object c = getHandle.invoke(chunk);
            Object w = this.methodGetWorld.of(c).call();
            Class<? extends Object> clazz = c.getClass();
            Field sf = clazz.getDeclaredField("sections");
            sf.setAccessible(true);
            Field tf = clazz.getDeclaredField("tileEntities");
            Field entitySlices = clazz.getDeclaredField("entitySlices");
            Object[] sections = (Object[]) sf.get(c);
            Map<?, ?> tiles = (Map<?, ?>) tf.get(c);
            Collection<?>[] entities = (Collection<?>[]) entitySlices.get(c);

            Method xm = null;
            Method ym = null;
            Method zm = null;
            // Trim tiles
            Collection tickList = ((Collection) this.tileEntityListTick.of(w).get());
            Set<Map.Entry<?, ?>> entrySet = (Set<Map.Entry<?, ?>>) (Set<?>) tiles.entrySet();
            Iterator<Map.Entry<?, ?>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> tile = iterator.next();
                Object pos = tile.getKey();
                if (xm == null) {
                    Class<?> clazz2 = pos.getClass().getSuperclass();
                    xm = clazz2.getDeclaredMethod("getX");
                    ym = clazz2.getDeclaredMethod("getY");
                    zm = clazz2.getDeclaredMethod("getZ");
                }
                int lx = (int) xm.invoke(pos) & 15;
                int ly = (int) ym.invoke(pos);
                int lz = (int) zm.invoke(pos) & 15;
                int j = MainUtil.CACHE_I[ly][lx][lz];
                int k = MainUtil.CACHE_J[ly][lx][lz];
                char[] array = fs.getIdArray(j);
                if (array == null) {
                    continue;
                }
                if (array[k] != 0) {
                    tickList.remove(tile.getValue());
                    iterator.remove();
                }
            }

            // Trim entities
            for (int i = 0; i < 16; i++) {
                if (entities[i] != null && fs.getCount(i) >= 4096) {
                    entities[i].clear();
                }
            }

            // Efficiently merge sections
            for (int j = 0; j < sections.length; j++) {
                if (fs.getCount(j) == 0) {
                    continue;
                }
                char[] newArray = fs.getIdArray(j);
                if (newArray == null) {
                    continue;
                }
                Object section = sections[j];
                if (section == null || fs.getCount(j) >= 4096) {
                    section = sections[j] = newChunkSection(j << 4, flag, fs.getIdArray(j));
                    continue;
                }
                Object currentArray = getBlocks(section);
                ReflectionUtils.RefMethod.RefExecutor setType = this.methodSetType.of(section);
                boolean fill = true;
                for (int k = 0; k < newArray.length; k++) {
                    char n = newArray[k];
                    switch (n) {
                        case 0:
                            fill = false;
                            continue;
                        case 1: {
                            fill = false;
                            int x = MainUtil.x_loc[j][k];
                            int y = MainUtil.y_loc[j][k];
                            int z = MainUtil.z_loc[j][k];
                            setType.call(x, y & 15, z, this.air);
                            continue;
                        }
                        default:
                            int x = MainUtil.x_loc[j][k];
                            int y = MainUtil.y_loc[j][k];
                            int z = MainUtil.z_loc[j][k];
                            int id = n >> 4;
                            int data = n & 15;
                            Object iBlock = this.methodGetByCombinedId.call((int) (id & 0xFFF) + (data << 12));
                            setType.call(x, y & 15, z, iBlock);
                    }
                }
                if (fill) {
                    fs.setCount(j, Short.MAX_VALUE);
                }
            }
            // Clear
        } catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        fixLighting(chunk, fs, true);
        refreshChunk(fs.getX(), fs.getZ());
    }

    public Object newChunkSection(int i, boolean flag, char[] ids) throws ReflectiveOperationException {
        return this.classChunkSectionConstructor.create(i, flag, ids);
    }

    public Object getBlocks(Object obj) {
        return this.methodGetBlocks.of(obj).call();
    }

    @Override
    public void fixChunkLighting(int x, int z) {
        Object c = this.methodGetHandleChunk.of(getChunk(x, z)).call();
        this.methodInitLighting.of(c).call();
    }

    public boolean fixLighting(Chunk chunk, CharLocalChunk_1_8_3 bc, boolean fixAll) {
        try {
            if (!chunk.isLoaded()) {
                chunk.load(false);
            } else {
                chunk.unload(true, false);
                chunk.load(false);
            }

            // Initialize lighting
            Object c = this.methodGetHandleChunk.of(chunk).call();

            ChunkWrapper wrapper = new ChunkWrapper(getWorld(), bc.getX(), bc.getZ());
            Object[] result = disableLighting(chunk);
            enableLighting(result);

            this.methodInitLighting.of(c).call();

            if (bc.getTotalRelight() != 0 || fixAll) {
                Object[] sections = (Object[]) this.fieldSections.of(c).get();
                Object w = this.fieldWorld.of(c).get();

                int X = chunk.getX() << 4;
                int Z = chunk.getZ() << 4;
                ReflectionUtils.RefMethod.RefExecutor relight = this.methodW.of(w);
                for (int j = 0; j < sections.length; j++) {
                    Object section = sections[j];
                    if (section == null) {
                        continue;
                    }
                    if (bc.getRelight(j) == 0 && !fixAll || bc.getCount(j) == 0 || bc.getCount(j) >= 4096 && bc.getAir(j) == 0) {
                        continue;
                    }
                    char[] array = bc.getIdArray(j);
                    if (array != null) {
                        int l = PseudoRandom.random.random(2);
                        for (int k = 0; k < array.length; k++) {
                            int i = array[k];
                            if (i < 16) {
                                continue;
                            }
                            short id = (short) (i >> 4);
                            switch (id) { // Lighting
                                default:
                                    if (!fixAll) {
                                        continue;
                                    }
                                    if ((k & 1) == l) {
                                        l = 1 - l;
                                        continue;
                                    }
                                case 10:
                                case 11:
                                case 39:
                                case 40:
                                case 50:
                                case 51:
                                case 62:
                                case 74:
                                case 76:
                                case 89:
                                case 122:
                                case 124:
                                case 130:
                                case 138:
                                case 169:
                                    int x = MainUtil.x_loc[j][k];
                                    int y = MainUtil.y_loc[j][k];
                                    int z = MainUtil.z_loc[j][k];
                                    if (isSurrounded(bc.blocks, x, y, z)) {
                                        continue;
                                    }
                                    Object pos = this.classBlockPositionConstructor.create(X + x, y, Z + z);
                                    relight.call(pos);
                            }
                        }
                    }
                }
            }
            resetLighting(result);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void refreshChunk(int x, int z) {
        getBukkitWorld().refreshChunk(x, z);
    }

    public boolean isSurrounded(char[][] sections, int x, int y, int z) {
        return isSolid(getId(sections, x, y + 1, z))
                && isSolid(getId(sections, x + 1, y - 1, z))
                && isSolid(getId(sections, x - 1, y, z))
                && isSolid(getId(sections, x, y, z + 1))
                && isSolid(getId(sections, x, y, z - 1));
    }

    public boolean isSolid(int i) {
        if (i != 0) {
            Material material = Material.getMaterial(i);
            return material != null && Material.getMaterial(i).isOccluding();
        }
        return false;
    }

    public int getId(char[] section, int x, int y, int z) {
        if (section == null) {
            return 0;
        }
        int j = MainUtil.CACHE_J[y][x][z];
        return section[j] >> 4;
    }

    public int getId(char[][] sections, int x, int y, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return 1;
        }
        if (y < 0 || y > 255) {
            return 1;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        char[] section = sections[i];
        if (section == null) {
            return 0;
        }
        return getId(section, x, y, z);
    }
}
