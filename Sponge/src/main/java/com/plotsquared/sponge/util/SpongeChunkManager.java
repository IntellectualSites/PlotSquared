package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class SpongeChunkManager extends ChunkManager {
    
    @Override
    public int[] countEntities(final Plot plot) {
        final Location pos1 = plot.getBottomAbs();
        final Location pos2 = plot.getTopAbs();
        final World world = SpongeUtil.getWorld(pos1.getWorld());
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        final int[] count = new int[6];
        world.getEntities(entity -> {
            final org.spongepowered.api.world.Location loc = entity.getLocation();
            final int x = loc.getBlockX();
            if ((x >= bx) && (x <= tx)) {
                final int z = loc.getBlockZ();
                if ((z >= bz) && (z <= tz)) {
                    count[0]++;
                    if (entity instanceof Living) {
                        count[3]++;
                        if (entity instanceof Animal) {
                            count[1]++;
                        } else if (entity instanceof Monster) {
                            count[2]++;
                        }
                    } else {
                        count[4]++;
                    }
                }
            }
            return false;
        });
        
        return count;
    }
    
    @Override
    public boolean loadChunk(final String world, final ChunkLoc loc, final boolean force) {
        final World worldObj = SpongeUtil.getWorld(world);
        return worldObj.loadChunk(loc.x << 4, 0, loc.z << 4, force).isPresent();
    }
    
    @Override
    public Set<ChunkLoc> getChunkChunks(final String world) {
        // TODO save world;
        return super.getChunkChunks(world);
    }

    @Override
    public void regenerateChunk(final String world, final ChunkLoc loc) {
        final World spongeWorld = SpongeUtil.getWorld(world);
        final net.minecraft.world.World nmsWorld = (net.minecraft.world.World) spongeWorld;
        final Optional<Chunk> chunkOpt = spongeWorld.getChunk(loc.x, 0, loc.z);
        if (chunkOpt.isPresent()) {
            try {
                Chunk spongeChunk = chunkOpt.get();
                IChunkProvider provider = nmsWorld.getChunkProvider();
                if (!(provider instanceof ChunkProviderServer)) {
                    PS.debug("Not valid world generator for: " + world);
                    return;
                }
                ChunkProviderServer chunkServer = (ChunkProviderServer) provider;
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
                }
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        // TODO copy a region
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public void clearAllEntities(final Location pos1, final Location pos2) {
        final String worldname = pos1.getWorld();
        final World world = SpongeUtil.getWorld(worldname);
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        world.getEntities(new Predicate<Entity>() {
            @Override
            public boolean test(final Entity entity) {
                final org.spongepowered.api.world.Location loc = entity.getLocation();
                final int x = loc.getBlockX();
                if ((x >= bx) && (x <= tx)) {
                    final int z = loc.getBlockZ();
                    if ((z >= bz) && (z <= tz)) {
                        entity.remove();
                    }
                }
                return false;
            }
        });
    }
    
    @Override
    public void swap(Location bot1, Location top1, Location bot2, Location top2, Runnable whenDone) {
        // TODO swap region
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public void unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe) {
        final World worldObj = SpongeUtil.getWorld(world);
        final Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            worldObj.unloadChunk(chunk.get());
        }
    }
    
    @Override
    public boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment, Runnable whenDone) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
}
