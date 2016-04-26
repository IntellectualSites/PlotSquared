package com.plotsquared.bukkit.util.block;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod.RefExecutor;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class FastQueue_1_9 extends SlowQueue {

    private final Object air;
//    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final RefMethod methodGetHandleChunk;
    private final RefMethod methodInitLighting;
    private final RefConstructor classBlockPositionConstructor;
    private final RefConstructor classChunkSectionConstructor;
    private final RefMethod methodW;
    private final RefMethod methodAreNeighborsLoaded;
    private final RefField fieldSections;
    private final RefField fieldWorld;
    private final RefMethod methodGetBlocks;
    private final RefMethod methodGetType;
    private final RefMethod methodSetType;
    private final RefMethod methodGetCombinedId;
    private final RefMethod methodGetByCombinedId;
    private final RefMethod methodGetWorld;

    private final RefField tileEntityListTick;


    public FastQueue_1_9() throws RuntimeException {
        RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
        this.classBlockPositionConstructor = classBlockPosition.getConstructor(int.class, int.class, int.class);
        RefClass classWorld = getRefClass("{nms}.World");
        this.tileEntityListTick = classWorld.getField("tileEntityListTick");
        this.methodGetWorld = classChunk.getMethod("getWorld");
        this.methodW = classWorld.getMethod("w", classBlockPosition.getRealClass());
        this.fieldSections = classChunk.getField("sections");
        this.fieldWorld = classChunk.getField("world");
        RefClass classBlock = getRefClass("{nms}.Block");
        RefClass classIBlockData = getRefClass("{nms}.IBlockData");
        this.methodGetCombinedId = classBlock.getMethod("getCombinedId", classIBlockData.getRealClass());
        this.methodGetByCombinedId = classBlock.getMethod("getByCombinedId", int.class);
        RefClass classChunkSection = getRefClass("{nms}.ChunkSection");
        this.methodGetBlocks = classChunkSection.getMethod("getBlocks");
        this.methodGetType = classChunkSection.getMethod("getType", int.class, int.class, int.class);
        this.methodSetType = classChunkSection.getMethod("setType", int.class, int.class, int.class, classIBlockData.getRealClass());
        this.methodAreNeighborsLoaded = classChunk.getMethod("areNeighborsLoaded", int.class);
        this.classChunkSectionConstructor = classChunkSection.getConstructor(int.class, boolean.class, char[].class);
        this.air = this.methodGetByCombinedId.call(0);
        MainUtil.initCache();
    }

    /**
     * This should be overridden by any specialized queues
     * @param plotChunk
     */
    @Override
    public void execute(PlotChunk<Chunk> plotChunk) {
        final FastChunk_1_9 fs = (FastChunk_1_9) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        World world = chunk.getWorld();
        ChunkWrapper wrapper = plotChunk.getChunkWrapper();
        chunk.load(true);
        try {
            boolean flag = world.getEnvironment() == Environment.NORMAL;

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
            HashMap<?, ?> tiles = (HashMap<?, ?>) tf.get(c);
            Collection<?>[] entities = (Collection<?>[]) entitySlices.get(c);

            Method xm = null;
            Method ym = null;
            Method zm = null;
            // Trim tiles
            boolean removed = false;
            Set<Entry<?, ?>> entrySet = (Set<Entry<?, ?>>) (Set<?>) tiles.entrySet();
            Iterator<Entry<?, ?>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry<?, ?> tile = iterator.next();
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
                int[] array = fs.getIdArray(j);
                if (array == null) {
                    continue;
                }
                if (array[k] != 0) {
                    removed = true;
                    iterator.remove();
                }
            }
            if (removed) {
                ((Collection) this.tileEntityListTick.of(w).get()).clear();
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
                int[] newArray = fs.getIdArray(j);
                if (newArray == null) {
                    continue;
                }
                Object section = sections[j];
                if (section == null || fs.getCount(j) >= 4096) {
                    char[] array = new char[4096];
                    for (int i = 0; i < newArray.length; i++) {
                        int combined = newArray[i];
                        int id = combined & 4095;
                        int data = combined >> 12;
                        array[i] = (char) ((id << 4) + data);
                    }
                    section = sections[j] = newChunkSection(j << 4, flag, array);
                    continue;
                }
                Object currentArray = getBlocks(section);
                RefExecutor setType = this.methodSetType.of(section);
                boolean fill = true;
                for (int k = 0; k < newArray.length; k++) {
                    int n = newArray[k];
                    switch (n) {
                        case 0:
                            fill = false;
                            continue;
                        case -1: {
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
                            Object iBlock = this.methodGetByCombinedId.call((int) n);
                            setType.call(x, y & 15, z, iBlock);
                    }
                }
                if (fill) {
                    fs.setCount(j, Short.MAX_VALUE);
                }
            }
            // Clear
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException |
                NoSuchFieldException e) {
            e.printStackTrace();
        }
        int[][] biomes = fs.biomes;
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
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                sendChunk(fs.getChunkWrapper().world, Arrays.asList(new ChunkLoc(fs.getX(), fs.getZ())));
            }
        }, 1);
    }

    public Object newChunkSection(int i, boolean flag, char[] ids) {
        return this.classChunkSectionConstructor.create(i, flag, ids);
    }

    public Object getBlocks(Object obj) {
        return this.methodGetBlocks.of(obj).call();
    }

    /**
     * This should be overridden by any specialized queues
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new FastChunk_1_9(wrap);
    }

    /**
     * This should be overridden by any specialized queues
     * @param pc
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> pc, boolean fixAll) {
        try {
            FastChunk_1_9 bc = (FastChunk_1_9) pc;
            Chunk chunk = bc.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load(false);
            } else {
                chunk.unload(true, true);
                chunk.load(false);
            }

            // Initialize lighting
            Object c = this.methodGetHandleChunk.of(chunk).call();

            if (fixAll && !(boolean) this.methodAreNeighborsLoaded.of(c).call(1)) {
                World world = chunk.getWorld();
                ChunkWrapper wrapper = bc.getChunkWrapper();
                String worldName = wrapper.world;
                for (int x = wrapper.x - 1; x <= wrapper.x + 1; x++) {
                    for (int z = wrapper.z - 1; z <= wrapper.z + 1; z++) {
                        if (x != 0 && z != 0) {
                            Chunk other = world.getChunkAt(x, z);
                            while (!other.isLoaded()) {
                                other.load(true);
                            }
                            ChunkManager.manager.loadChunk(worldName, new ChunkLoc(x, z), true);
                        }
                    }
                }
            }

            this.methodInitLighting.of(c).call();

            if (bc.getTotalRelight() == 0 && !fixAll) {
                return true;
            }

            Object[] sections = (Object[]) this.fieldSections.of(c).get();
            Object w = this.fieldWorld.of(c).get();

            int X = chunk.getX() << 4;
            int Z = chunk.getZ() << 4;

            RefExecutor relight = this.methodW.of(w);
            for (int j = 0; j < sections.length; j++) {
                Object section = sections[j];
                if (section == null) {
                    continue;
                }
                if (bc.getRelight(j) == 0 && !fixAll || bc.getCount(j) == 0 || bc.getCount(j) >= 4096 && bc.getAir(j) == 0) {
                    continue;
                }
                int[] array = bc.getIdArray(j);
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
                                if (isSurrounded(bc.getIdArrays(), x, y, z)) {
                                    continue;
                                }
                                Object pos = this.classBlockPositionConstructor.create(X + x, y, Z + z);
                                relight.call(pos);
                        }
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSurrounded(int[][] sections, int x, int y, int z) {
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

    public int getId(int[][] sections, int x, int y, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return 1;
        }
        if (y < 0 || y > 255) {
            return 1;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        int[] section = sections[i];
        if (section == null) {
            return 0;
        }
        int j = MainUtil.CACHE_J[y][x][z];
        return section[j];
    }

    public int getId(Object section, int x, int y, int z) {
        int j = MainUtil.CACHE_J[y][x][z];
        Object iBlock = this.methodGetType.of(section).call(x, y & 15, z);
        return (int) this.methodGetCombinedId.call(iBlock);
    }

    public int getId(Object[] sections, int x, int y, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return 1;
        }
        if (y < 0 || y > 255) {
            return 1;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        Object section = sections[i];
        if (section == null) {
            return 0;
        }
        return getId(section, x, y, z);
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
            worldObj.refreshChunk(loc.x, loc.z);
        }
    }
}
