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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.intellectualcrafters.jnbt.ByteArrayTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.IntTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.StringTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.schematic.StateWrapper;

/**
 * Schematic Handler
 *
 * @author Citymonstret
 * @author Empire92
 */
public class BukkitSchematicHandler extends SchematicHandler {

    @Override
    public void getCompoundTag(final String world, final Location pos1, final Location pos2, RunnableVal<CompoundTag> whenDone) {
        
        // create schematic one chunk at a time
        // load chunk sync
        // get blocks async
        // add to schematic async
        // save final async
        
        int i = 0;
        int j = 0;
        try {
            for (i = (pos1.getX() / 16) * 16; i < (16 + ((pos2.getX() / 16) * 16)); i += 16) {
                for (j = (pos1.getZ() / 16) * 16; j < (16 + ((pos2.getZ() / 16) * 16)); j += 16) {
                    boolean result = ChunkManager.manager.loadChunk(world, new ChunkLoc(i, j));
                    if (!result) {
                        PS.log("&cIllegal selection. Cannot save non-existent chunk at " + (i / 16) + ", " + (j / 16));
                        whenDone.run();
                        return;
                    }
                }
            }
        } catch (final Exception e) {
            PS.log("&cIllegal selection. Cannot save corrupt chunk at " + (i / 16) + ", " + (j / 16));
            whenDone.run();
            return;
        }
        final int width = (pos2.getX() - pos1.getX()) + 1;
        final int height = (pos2.getY() - pos1.getY()) + 1;
        final int length = (pos2.getZ() - pos1.getZ()) + 1;
        final HashMap<String, Tag> schematic = new HashMap<>();
        schematic.put("Width", new ShortTag("Width", (short) width));
        schematic.put("Length", new ShortTag("Length", (short) length));
        schematic.put("Height", new ShortTag("Height", (short) height));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", 0));
        schematic.put("WEOriginY", new IntTag("WEOriginY", 0));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", 0));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", 0));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", 0));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", 0));
        final byte[] blocks = new byte[width * height * length];
        byte[] addBlocks = null;
        final byte[] blockData = new byte[width * height * length];
        final int sx = pos1.getX();
        pos2.getX();
        final int sz = pos1.getZ();
        pos2.getZ();
        final int sy = pos1.getY();
        pos2.getY();
        List<Tag> tileEntities = new ArrayList<Tag>();
        
        World worldObj = Bukkit.getWorld(world);

        
        for (int y = 0; y < height; y++) {
            int i1 = (y * width * length);
            int syy = sy + y;
            for (int z = 0; z < length; z++) {
                int i2 = i1 + (z * width);
                int szz = sz + z;
                for (int x = 0; x < width; x++) {
                    final int index = i2 + x;
                    Block block = worldObj.getBlockAt(sx + x, syy, szz);
                    int id = block.getTypeId();
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
                        case 55:
                        case 56:
                        case 57:
                        case 58:
                        case 60:
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
                        case 122:
                        case 129:
                        case 133:
                        case 165:
                        case 166:
                        case 169:
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
                        case 192: {
                            break;
                        }
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
                        case 116:
                        case 28:
                        case 66:
                        case 157:
                        case 61:
                        case 62:
                        case 140:
                        case 146:
                        case 149:
                        case 150:
                        case 158:
                        case 23:
                        case 123:
                        case 124:
                        case 29:
                        case 33:
                        case 151:
                        case 178: {
                            // TODO implement fully
                            BlockState state = block.getState();
                            if (state != null) {
                                StateWrapper wrapper = new StateWrapper(state);
                                CompoundTag rawTag = wrapper.getTag();
                                if (rawTag != null) {
                                    Map<String, Tag> values = new HashMap<String, Tag>();
                                    for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                        values.put(entry.getKey(), entry.getValue());
                                    }
                                    values.put("id", new StringTag("id", wrapper.getId()));
                                    values.put("x", new IntTag("x", x));
                                    values.put("y", new IntTag("y", y));
                                    values.put("z", new IntTag("z", z));
                                    CompoundTag tileEntityTag = new CompoundTag(values);
                                    tileEntities.add(tileEntityTag);
                                }
                            }
                        }
                        default: {
                            blockData[index] = block.getData();
                        }
                    }
                    if (id > 255) {
                        if (addBlocks == null) {
                            addBlocks = new byte[(blocks.length >> 1) + 1];
                        }
                        addBlocks[index >> 1] = (byte) (((index & 1) == 0) ? (addBlocks[index >> 1] & 0xF0) | ((id >> 8) & 0xF) : (addBlocks[index >> 1] & 0xF) | (((id >> 8) & 0xF) << 4));
                    }
                    blocks[index] = (byte) id;
                }
            }
        }
        
        
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
        
        if (addBlocks != null) {
            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
        }
        
        
        whenDone.value = new CompoundTag("Schematic", schematic);
        whenDone.run();
    }

    
}
