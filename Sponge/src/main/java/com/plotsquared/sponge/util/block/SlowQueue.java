package com.plotsquared.sponge.util.block;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.PlotQueue;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SlowQueue implements PlotQueue<Chunk> {
    
    private final ConcurrentHashMap<ChunkWrapper, PlotChunk<Chunk>> blocks = new ConcurrentHashMap<>();

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
            Iterator<Entry<ChunkWrapper, PlotChunk<Chunk>>> iter = this.blocks.entrySet().iterator();
            PlotChunk<Chunk> toReturn = iter.next().getValue();
            if (SetQueue.IMP.isWaiting()) {
                return null;
            }
            iter.remove();
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
        World spongeWorld = SpongeUtil.getWorld(world);
        net.minecraft.world.World nmsWorld = (net.minecraft.world.World) spongeWorld;
        Optional<Chunk> chunkOpt = spongeWorld.getChunk(loc.x, 0, loc.z);
        if (chunkOpt.isPresent()) {
            try {
                Chunk spongeChunk = chunkOpt.get();
                IChunkProvider provider = nmsWorld.getChunkProvider();
                if (!(provider instanceof ChunkProviderServer)) {
                    PS.debug("Not valid world generator for: " + world);
                }
/*                ChunkProviderServer chunkServer = (ChunkProviderServer) provider;
                IChunkProvider chunkProvider = chunkServer.serverChunkGenerator;

                long pos = ChunkCoordIntPair.chunkXZ2Int(loc.x, loc.z);
                net.minecraft.world.chunk.Chunk mcChunk = (net.minecraft.world.chunk.Chunk) spongeChunk;
                if (chunkServer.chunkExists(loc.x, loc.z)) {
                    mcChunk = chunkServer.loadChunk(loc.x, loc.z);
                    mcChunk.onChunkUnload();
                }
                Field fieldDroppedChunksSet;
                try {
                    fieldDroppedChunksSet = chunkServer.getClass().getField("droppedChunksSet");
                } catch (Throwable t) {
                    fieldDroppedChunksSet = ReflectionUtils.findField(chunkServer.getClass(), Set.class);
                }
                Set<Long> set = (Set<Long>) fieldDroppedChunksSet.get(chunkServer);
                set.remove(pos);
                ReflectionUtils.findField(chunkServer.getClass(),)
                chunkServer.id2ChunkMap.remove(pos);
                mcChunk = chunkProvider.provideChunk(loc.x, loc.z);
                chunkServer.id2ChunkMap.add(pos, mcChunk);
                chunkServer.loadedChunks.add(mcChunk);
                if (mcChunk != null) {
                    mcChunk.onChunkLoad();
                    mcChunk.populateChunk(chunkProvider, chunkProvider, loc.x, loc.z);
                    SetQueue.IMP.queue.sendChunk(world, Arrays.asList(loc));
                }
                else {
                    PS.debug("CHUNK IS NULL!?");
                }*/
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This should be overriden by any specialized queues.
     * @param plotChunk
     */
    public void execute(PlotChunk<Chunk> plotChunk) {
        SlowChunk sc = (SlowChunk) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        chunk.loadChunk(true);
        Vector3i min = chunk.getBlockMin();
        int bx = min.getX();
        int bz = min.getZ();
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
                BlockState state = SpongeUtil.getBlockState(newBlock.id, newBlock.data);
                chunk.setBlock(bx + x, y, bz + z, state, false);
            }
        }
        int[][] biomes = sc.biomes;
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
                    chunk.setBiome(bx + x, bz + z, SpongeUtil.getBiome(biome));
                }
            }
        }
    }
    
    /**
     * This should be overriden by any specialized queues.
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new SlowChunk(wrap);
    }
    
    /**
     * This should be overriden by any specialized queues.
     * @param fixAll
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> chunk, boolean fixAll) {
        // Do nothing
        return true;
    }
    
    /**
     * This should be overriden by any specialized queues.
     * @param locs
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locs) {
        // Do nothing
    }
}
