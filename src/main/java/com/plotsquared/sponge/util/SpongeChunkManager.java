package com.plotsquared.sponge.util;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;

public class SpongeChunkManager extends ChunkManager {
    
    @Override
    public void setChunk(final ChunkWrapper loc, final PlotBlock[][] result) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public int[] countEntities(final Plot plot) {
        final Location pos1 = plot.getBottomAbs();
        final Location pos2 = plot.getTopAbs();
        
        final String worldname = pos1.getWorld();
        final World world = SpongeUtil.getWorld(worldname);
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        final int[] count = new int[5];
        world.getEntities(new Predicate<Entity>() {
            @Override
            public boolean test(final Entity entity) {
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
            }
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
        final World worldObj = SpongeUtil.getWorld(world);
        final Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            // TODO regenerate chunk
        }
    }
    
    @Override
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        // TODO copy a region
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone) {
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
        
    }
    
    @Override
    public void unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe) {
        final World worldObj = SpongeUtil.getWorld(world);
        final Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            worldObj.unloadChunk(chunk.get());
        }
    }
    
}
