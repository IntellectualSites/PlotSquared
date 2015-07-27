////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.plotsquared.bukkit.util.bukkit;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.TaskManager;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.HashMap;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

/**
 * SetBlockFast class<br> Used to do fast world editing
 *
 * @author Empire92
 */
public class SetBlockFast extends BukkitSetBlockManager {
    private static final RefClass classBlock = getRefClass("{nms}.Block");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private static RefMethod methodGetHandle;
    private static RefMethod methodGetChunkAt;
    private static RefMethod methodA;
    private static RefMethod methodGetById;
    
    public static HashMap<ChunkLoc, Chunk> toUpdate = new HashMap<>();

    /**
     * Constructor
     *
     * @throws NoSuchMethodException
     */
    public SetBlockFast() throws NoSuchMethodException {
        methodGetHandle = classCraftWorld.getMethod("getHandle");
        methodGetChunkAt = classWorld.getMethod("getChunkAt", int.class, int.class);
        methodA = classChunk.getMethod("a", int.class, int.class, int.class, classBlock, int.class);
        methodGetById = classBlock.getMethod("getById", int.class);
        TaskManager.runTaskRepeat(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                update(toUpdate.values());
                toUpdate = new HashMap<>();
            }
        }, 20);
    }

    private ChunkLoc lastLoc = null;
    
    /**
     * Set the block at the location
     *
     * @param world   World in which the block should be set
     * @param x       X Coordinate
     * @param y       Y Coordinate
     * @param z       Z Coordinate
     * @param blockId Block ID
     * @param data    Block Data Value
     *
     */
    @Override
    public void set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data) {
        if (blockId == -1) {
            world.getBlockAt(x, y, z).setData(data, false);
            return;
        }
        int X = x >> 4;
        int Z = z >> 4;
        ChunkLoc loc = new ChunkLoc(X, Z);
        if (!loc.equals(lastLoc)) {
            Chunk chunk = toUpdate.get(loc);
            if (chunk == null) {
                chunk = world.getChunkAt(X, Z);
                toUpdate.put(loc, chunk);
            }
            chunk.load(false);
        }
        
        final Object w = methodGetHandle.of(world).call();
        final Object chunk = methodGetChunkAt.of(w).call(x >> 4, z >> 4);
        final Object block = methodGetById.of(null).call(blockId);
        methodA.of(chunk).call(x & 0x0f, y, z & 0x0f, block, data);
    }

    /**
     * Update chunks
     *
     * @param chunks list of chunks to update
     */
    @Override
    public void update(final Collection<Chunk> chunks) {
        if (chunks.size() == 0) {
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
            SendChunk.sendChunk(chunks);
        } catch (final Throwable e) {
            MainUtil.canSendChunk = false;
        }
    }
}
