package com.plotsquared.bukkit.util.block;

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
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.SendChunk;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;


import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public class FastQueue_1_8_3 extends SlowQueue {

    private final SendChunk sendChunk;
    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final RefMethod methodGetHandleChunk;
    private final RefMethod methodGetHandleWorld;
    private final RefMethod methodInitLighting;
    private final RefConstructor classBlockPositionConstructor;
    private final RefConstructor classChunkSectionConstructor;
    private final RefMethod methodX;
    private final RefMethod methodAreNeighborsLoaded;
    private final RefField fieldSections;
    private final RefField fieldWorld;
    private final RefMethod methodGetIdArray;
    private final RefMethod methodGetWorld;
    private final RefField tileEntityListTick;

    public FastQueue_1_8_3() throws RuntimeException {
        RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
        this.classBlockPositionConstructor = classBlockPosition.getConstructor(int.class, int.class, int.class);
        RefClass classWorld = getRefClass("{nms}.World");
        this.methodX = classWorld.getMethod("x", classBlockPosition.getRealClass());
        this.fieldSections = classChunk.getField("sections");
        this.fieldWorld = classChunk.getField("world");
        RefClass classChunkSection = getRefClass("{nms}.ChunkSection");
        this.methodGetIdArray = classChunkSection.getMethod("getIdArray");
        this.methodAreNeighborsLoaded = classChunk.getMethod("areNeighborsLoaded", int.class);
        this.classChunkSectionConstructor = classChunkSection.getConstructor(int.class, boolean.class, char[].class);
        this.tileEntityListTick = classWorld.getField("tileEntityList");
        this.methodGetHandleWorld = classCraftWorld.getMethod("getHandle");
        this.methodGetWorld = classChunk.getMethod("getWorld");
        this.sendChunk = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (FastQueue_1_8_3.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<>();
                Iterator<Entry<ChunkWrapper, Chunk>> i = FastQueue_1_8_3.this.toUpdate.entrySet().iterator();
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

    /**
     * This should be overridden by any specialized queues.
     * @param plotChunk
     */
    @Override
    public void execute(PlotChunk<Chunk> plotChunk) {
        FastChunk_1_8_3 fs = (FastChunk_1_8_3) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        World world = chunk.getWorld();
        ChunkWrapper wrapper = plotChunk.getChunkWrapper();
        if (!this.toUpdate.containsKey(wrapper)) {
            this.toUpdate.put(wrapper, chunk);
        }
        chunk.load(true);
        try {
            boolean flag = world.getEnvironment() == Environment.NORMAL;

            // Sections
            Method getHandle = chunk.getClass().getDeclaredMethod("getHandle");
            Object c = getHandle.invoke(chunk);
            Object w = this.methodGetWorld.of(c).call();
            Class<? extends Object> clazz = c.getClass();
            Field sections1 = clazz.getDeclaredField("sections");
            sections1.setAccessible(true);
            Field tileEntities = clazz.getDeclaredField("tileEntities");
            Field entitySlices = clazz.getDeclaredField("entitySlices");
            Object[] sections = (Object[]) sections1.get(c);
            HashMap<?, ?> tiles = (HashMap<?, ?>) tileEntities.get(c);
            Collection<?>[] entities = (Collection<?>[]) entitySlices.get(c);

            Method getX = null;
            Method getY = null;
            Method getZ = null;

            // Trim tiles
            boolean removed = false;
            Set<Entry<?, ?>> entrySet = (Set<Entry<?, ?>>) (Set<?>) tiles.entrySet();
            Iterator<Entry<?, ?>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry<?, ?> tile = iterator.next();
                Object pos = tile.getKey();
                if (getX == null) {
                    Class<? extends Object> clazz2 = pos.getClass().getSuperclass();
                    getX = clazz2.getDeclaredMethod("getX");
                    getY = clazz2.getDeclaredMethod("getY");
                    getZ = clazz2.getDeclaredMethod("getZ");
                }
                int lx = (int) getX.invoke(pos) & 15;
                int ly = (int) getY.invoke(pos);
                int lz = (int) getZ.invoke(pos) & 15;
                int j = MainUtil.CACHE_I[ly][lx][lz];
                int k = MainUtil.CACHE_J[ly][lx][lz];
                char[] array = fs.getIdArray(j);
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
                if ((entities[i] != null) && (fs.getCount(i) >= 4096)) {
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
                if ((section == null) || (fs.getCount(j) >= 4096)) {
                    section = sections[j] = newChunkSection(j << 4, flag, newArray);
                    continue;
                }
                char[] currentArray = getIdArray(section);
                boolean fill = true;
                for (int k = 0; k < newArray.length; k++) {
                    char n = newArray[k];
                    switch (n) {
                        case 0:
                            fill = false;
                            continue;
                        case 1:
                            fill = false;
                            currentArray[k] = 0;
                            continue;
                        default:
                            currentArray[k] = n;
                            continue;
                    }
                }
                if (fill) {
                    fs.setCount(j, Short.MAX_VALUE);
                }
            }
            // Clear
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException
                | NoSuchFieldException e) {
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
    }

    public Object newChunkSection(int i, boolean flag, char[] ids) {
        return this.classChunkSectionConstructor.create(i, flag, ids);
    }

    public char[] getIdArray(Object obj) {
        return (char[]) this.methodGetIdArray.of(obj).call();
    }

    /**
     * This should be overridden by any specialized queues.
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new FastChunk_1_8_3(wrap);
    }

    /**
     * This should be overridden by any specialized queues
     * @param plotChunk
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> plotChunk, boolean fixAll) {
        try {
            FastChunk_1_8_3 bc = (FastChunk_1_8_3) plotChunk;
            Chunk chunk = bc.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load(false);
            } else {
                chunk.unload(true, false);
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
/*
                if (!(boolean) methodAreNeighborsLoaded.of(c).call(1)) {
                    return false;
                }
*/
            }

            this.methodInitLighting.of(c).call();

            if (bc.getTotalRelight() == 0 && !fixAll) {
                return true;
            }

            Object[] sections = (Object[]) this.fieldSections.of(c).get();
            Object w = this.fieldWorld.of(c).get();

            int X = chunk.getX() << 4;
            int Z = chunk.getZ() << 4;

            RefExecutor relight = this.methodX.of(w);
            for (int j = 0; j < sections.length; j++) {
                Object section = sections[j];
                if (section == null) {
                    continue;
                }
                if ((bc.getRelight(j) == 0 && !fixAll) || bc.getCount(j) == 0 || (bc.getCount(j) >= 4096 && bc.getAir(j) == 0)) {
                    continue;
                }
                char[] array = getIdArray(section);
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
                            if (isSurrounded(sections, x, y, z)) {
                                continue;
                            }
                            Object pos = this.classBlockPositionConstructor.create(X + x, y, Z + z);
                            relight.call(pos);
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSurrounded(Object[] sections, int x, int y, int z) {
        return isSolid(getId(sections, x, y + 1, z))
                && isSolid(getId(sections, x + 1, y - 1, z))
                && isSolid(getId(sections, x - 1, y, z))
                && isSolid(getId(sections, x, y, z + 1))
                && isSolid(getId(sections, x, y, z - 1));
    }

    public boolean isSolid(int i) {
        return i != 0 && Material.getMaterial(i).isOccluding();
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
        char[] array = getIdArray(section);
        int j = MainUtil.CACHE_J[y][x][z];
        return array[j] >> 4;
    }

    /**
     * This should be overridden by any specialized queues.
     * @param world
     * @param locations
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locations) {
        for (ChunkLoc loc : locations) {
            ChunkWrapper wrapper = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            this.toUpdate.remove(wrapper);
        }
        this.sendChunk.sendChunk(world, locations);
    }
}
