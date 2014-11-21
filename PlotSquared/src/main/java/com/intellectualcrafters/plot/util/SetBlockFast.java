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

package com.intellectualcrafters.plot.util;

import java.util.ArrayList;

import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

/**
 * SetBlockFast class<br>
 * Used to do fast world editing
 *
 * @author Empire92
 */
public class SetBlockFast {

    private static final RefClass classBlock = getRefClass("{nms}.Block");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");

    private static RefMethod methodGetHandle;
    private static RefMethod methodGetChunkAt;
    private static RefMethod methodA;
    private static RefMethod methodGetById;

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
    }

    /**
     * Set the block at the location
     * @param world World in which the block should be set
     * @param x X Coordinate
     * @param y Y Coordinate
     * @param z Z Coordinate
     * @param blockId Block ID
     * @param data Block Data Value
     * @return true
     * @throws NoSuchMethodException
     */
    public static boolean set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data) throws NoSuchMethodException {

        final Object w = methodGetHandle.of(world).call();
        final Object chunk = methodGetChunkAt.of(w).call(x >> 4, z >> 4);
        final Object block = methodGetById.of(null).call(blockId);
        methodA.of(chunk).call(x & 0x0f, y, z & 0x0f, block, data);
        return true;
    }

    /**
     * Update chunks
     * @param player Player whose chunks we're updating
     */
    public static void update(final org.bukkit.entity.Player player) {
        if (!PlotHelper.canSendChunk) {
            
            final int distance = Bukkit.getViewDistance();
            for (int cx = -distance; cx < distance; cx++) {
                for (int cz = -distance; cz < distance; cz++) {
                    player.getWorld().refreshChunk(player.getLocation().getChunk().getX() + cx, player.getLocation().getChunk().getZ() + cz);
                }
            }
            
            return;
        }
        ArrayList<Chunk> chunks = new ArrayList<>();
        
        final int distance = Bukkit.getViewDistance();
        for (int cx = -distance; cx < distance; cx++) {
            for (int cz = -distance; cz < distance; cz++) {
                Chunk chunk = player.getWorld().getChunkAt(player.getLocation().getChunk().getX() + cx, player.getLocation().getChunk().getZ() + cz);
                chunks.add(chunk);
            }
        }
        
        try {
            SendChunk.sendChunk(chunks);
        }
        catch (Throwable e) {
            PlotHelper.canSendChunk = false;
        }
    }
}
