package com.plotsquared.sponge.util.block;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.sponge.util.SpongeUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.Map.Entry;

public class FastQueue extends SlowQueue {

    public HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    public final SendChunk chunkSender;

    public FastQueue() throws NoSuchMethodException, RuntimeException {
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                final ArrayList<Chunk> chunks = new ArrayList<Chunk>();
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
        chunkSender = new SendChunk();
        MainUtil.initCache();
    }

    public void update(final Collection<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            for (final Chunk chunk : chunks) {
                chunk.unloadChunk();
                chunk.loadChunk(false);
            }
            return;
        }
        try {
            chunkSender.sendChunk(chunks);
        } catch (final Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    /**
     * This should be overridden by any specialized queues
     * @param pc
     */
    @Override
    public void execute(PlotChunk<Chunk> pc) {
        FastChunk fs = (FastChunk) pc;
        Chunk spongeChunk = pc.getChunk();
        net.minecraft.world.World nmsWorld = (net.minecraft.world.World) spongeChunk.getWorld();
        ChunkWrapper wrapper = pc.getChunkWrapper();
        if (!toUpdate.containsKey(wrapper)) {
            toUpdate.put(wrapper, spongeChunk);
        }
        spongeChunk.loadChunk(true);
        try {
            final boolean flag = !nmsWorld.provider.getHasNoSky();
            // Sections
            net.minecraft.world.chunk.Chunk nmsChunk = (net.minecraft.world.chunk.Chunk) spongeChunk;
            ExtendedBlockStorage[] sections = nmsChunk.getBlockStorageArray();
            Map<BlockPos, TileEntity> tiles = nmsChunk.getTileEntityMap();
            ClassInheritanceMultiMap<Entity>[] entities = nmsChunk.getEntityLists();
            // Trim tiles
            Set<Entry<BlockPos, TileEntity>> entryset = tiles.entrySet();
            Iterator<Entry<BlockPos, TileEntity>> iter = entryset.iterator();
            while (iter.hasNext()) {
                Entry<BlockPos,TileEntity> tile = iter.next();
                BlockPos pos = tile.getKey();
                final int lx = pos.getX() & 15;
                final int ly = pos.getY();
                final int lz = pos.getZ() & 15;
                final int j = MainUtil.CACHE_I[ly][lx][lz];
                final int k = MainUtil.CACHE_J[ly][lx][lz];
                final char[] array = fs.getIdArray(j);
                if (array == null) {
                    continue;
                }
                if (array[k] != 0) {
                    iter.remove();
                }
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
                final char[] newArray = fs.getIdArray(j);
                if (newArray == null) {
                    continue;
                }
                ExtendedBlockStorage section = sections[j];
                if ((section == null) || (fs.getCount(j) >= 4096)) {
                    section = new ExtendedBlockStorage(j << 4, flag);
                    section.setData(newArray);
                    sections[j] = section;
                    continue;
                }
                final char[] currentArray = section.getData();
                boolean fill = true;
                for (int k = 0; k < newArray.length; k++) {
                    final char n = newArray[k];
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
        int[][] biomes = fs.biomes;
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
                    spongeChunk.setBiome(x, z, SpongeUtil.getBiome(biome));
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
        return new FastChunk(wrap);
    }

    /**
     * This should be overridden by any specialized queues
     * @param pc
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> pc, boolean fixAll) {
        try {
            FastChunk bc = (FastChunk) pc;
            final Chunk spongeChunk = bc.getChunk();
            final net.minecraft.world.chunk.Chunk nmsChunk = (net.minecraft.world.chunk.Chunk) spongeChunk;
            if (!spongeChunk.isLoaded()) {
                if (!spongeChunk.loadChunk(false)) {
                    return false;
                }
            } else {
                spongeChunk.unloadChunk();
                spongeChunk.loadChunk(false);
            }
            // TODO load adjaced chunks
            nmsChunk.generateSkylightMap();
            if ((bc.getTotalRelight() == 0 && !fixAll)) {
                return true;
            }
            ExtendedBlockStorage[] sections = nmsChunk.getBlockStorageArray();
            net.minecraft.world.World nmsWorld = nmsChunk.getWorld();

            final int X = pc.getX() << 4;
            final int Z = pc.getZ() << 4;


            for (int j = 0; j < sections.length; j++) {
                ExtendedBlockStorage section = sections[j];
                if (section == null) {
                    continue;
                }
                if ((bc.getRelight(j) == 0 && !fixAll) || bc.getCount(j) == 0 || (bc.getCount(j) >= 4096 && bc.getAir(j) == 0)) {
                    continue;
                }
                final char[] array = section.getData();
                int l = PseudoRandom.random.random(2);
                for (int k = 0; k < array.length; k++) {
                    final int i = array[k];
                    if (i < 16) {
                        continue;
                    }
                    final short id = (short) (i >> 4);
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
                            final int x = MainUtil.x_loc[j][k];
                            final int y = MainUtil.y_loc[j][k];
                            final int z = MainUtil.z_loc[j][k];
                            if (isSurrounded(sections, x, y, z)) {
                                continue;
                            }
                            BlockPos pos = new BlockPos(X + x, y, Z + z);
                            nmsWorld.checkLight(pos);
                    }
                }
            }
            return true;
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSurrounded(ExtendedBlockStorage[] sections, int x, int y, int z) {
        return isSolid(getId(sections, x, y + 1, z))
                && isSolid(getId(sections, x + 1, y - 1, z))
                && isSolid(getId(sections, x - 1, y, z))
                && isSolid(getId(sections, x, y, z + 1))
                && isSolid(getId(sections, x, y, z - 1));
    }

    public boolean isSolid(int i) {
        return i != 0 && Block.getBlockById(i).isOpaqueCube();
    }

    public int getId(ExtendedBlockStorage[] sections, int x, int y, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return 1;
        }
        if (y < 0 || y > 255) {
            return 1;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        ExtendedBlockStorage section = sections[i];
        if (section == null) {
            return 0;
        }
        char[] array = section.getData();
        int j = MainUtil.CACHE_J[y][x][z];
        return array[j] >> 4;
    }

    /**
     * This should be overridden by any specialized queues
     * @param world
     * @param locs
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locs) {
        World spongeWorld = SpongeUtil.getWorld(world);
        for (ChunkLoc loc : locs) {
            ChunkWrapper wrapper = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            if (!toUpdate.containsKey(wrapper)) {
                toUpdate.put(wrapper, spongeWorld.getChunk(loc.x, 0, loc.z).get());
            }
        }
    }
}
