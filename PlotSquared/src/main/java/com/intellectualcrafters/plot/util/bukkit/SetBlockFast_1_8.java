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
package com.intellectualcrafters.plot.util.bukkit;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;

/**
 * SetBlockFast class<br> Used to do fast world editing
 *
 * @author Empire92
 */
public class SetBlockFast_1_8 extends BukkitSetBlockManager {
    private static final RefClass classBlock = getRefClass("{nms}.Block");
    private static final RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
    private static final RefClass classIBlockData = getRefClass("{nms}.IBlockData");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private static RefMethod methodGetHandle;
    private static RefMethod methodGetChunkAt;
    private static RefMethod methodA;
    private static RefMethod methodGetByCombinedId;
    private static RefConstructor constructorBlockPosition;

    /**
     * Constructor
     *
     * @throws NoSuchMethodException
     */
    public SetBlockFast_1_8() throws NoSuchMethodException {
        constructorBlockPosition = classBlockPosition.getConstructor(int.class, int.class, int.class);
        methodGetByCombinedId = classBlock.getMethod("getByCombinedId", int.class);
        methodGetHandle = classCraftWorld.getMethod("getHandle");
        methodGetChunkAt = classWorld.getMethod("getChunkAt", int.class, int.class);
        methodA = classChunk.getMethod("a", classBlockPosition, classIBlockData);
    }

    private ChunkLoc lastLoc = null;
    
    /**
     * Set the block at the location
     *
     * @param world   World in which the block should be set
     * @param x       X Coordinate
     * @param y       Y Coordinate
     * @param z       Z Coordinate
     * @param id      Block ID
     * @param data    Block Data Value
     */
    @SuppressWarnings("deprecation")
    @Override
    public void set(final World world, final int x, final int y, final int z, final int id, final byte data) {
        // Start blockstate workaround //
        switch (id) {
            case 54:
            case 130:
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
            case 28:
            case 66:
            case 157:
            case 61:
            case 62:
            case 158:
            case 23:
            case 123:
            case 124:
            case 29:
            case 33:
            case 151:
            case 178: {
                final Block block = world.getBlockAt(x, y, z);
                if (block.getData() == data) {
                    if (block.getTypeId() != id) {
                        block.setTypeId(id, false);
                    }
                } else {
                    if (block.getTypeId() == id) {
                        block.setData(data, false);
                    } else {
                        block.setTypeIdAndData(id, data, false);
                    }
                }
                return;
            }
        }
        Block block = world.getBlockAt(x, y, z);
        int currentId = block.getTypeId();
        if (currentId == id) {
            switch(id) {
                case 0:
                case 2:
                case 4:
                case 13:
                case 14:
                case 15:
                case 20:
                case 21:
                case 22:
                case 24:
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
                case 50:
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
                case 75:
                case 76:
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
                case 192: {
                    return;
                }
            }
            if (block.getData() == data) {
                return;
            }
            block.setData(data);
            return;
        }
        switch(currentId) {
            case 54:
            case 130:
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
            case 28:
            case 66:
            case 157:
            case 61:
            case 62:
            case 158:
            case 23:
            case 123:
            case 124:
            case 29:
            case 33:
            case 151:
            case 178: {
                if (block.getData() == data) {
                    block.setTypeId(id, false);
                } else {
                    block.setTypeIdAndData(id, data, false);
                }
                return;
            }
        }
        // End blockstate workaround //
        
        int X = x >> 4;
        int Z = z >> 4;
        ChunkLoc loc = new ChunkLoc(X, Z);
        if (!loc.equals(lastLoc)) {
            world.loadChunk(X, Z, false);
            lastLoc = loc;
        }
        // check sign
        final Object w = methodGetHandle.of(world).call();
        final Object chunk = methodGetChunkAt.of(w).call(x >> 4, z >> 4);
        final Object pos = constructorBlockPosition.create(x & 0x0f, y, z & 0x0f);
        final Object combined = methodGetByCombinedId.of(null).call(id + (data << 12));
        methodA.of(chunk).call(pos, combined);
    }
    
    /**
     * Update chunks
     *
     * @param chunks chunks to be updated
     */
    @Override
    public void update(final List<Chunk> chunks) {
        if (chunks.size() == 0) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            final World world = chunks.get(0).getWorld();
            for (final Chunk chunk : chunks) {
                world.refreshChunk(chunk.getX(), chunk.getZ());
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
