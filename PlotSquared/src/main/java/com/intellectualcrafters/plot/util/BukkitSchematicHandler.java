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
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.schematic.StateWrapper;

/**
 * Schematic Handler
 *
 * @author Citymonstret
 * @author Empire92
 */
public class BukkitSchematicHandler extends SchematicHandler {

    @Override
    public CompoundTag getCompoundTag(final String world, final Location pos1, final Location pos2) {
        // loading chunks
        int i = 0;
        int j = 0;
        try {
            for (i = (pos1.getX() / 16) * 16; i < (16 + ((pos2.getX() / 16) * 16)); i += 16) {
                for (j = (pos1.getZ() / 16) * 16; j < (16 + ((pos2.getZ() / 16) * 16)); j += 16) {
                    boolean result = ChunkManager.manager.loadChunk(world, new ChunkLoc(i, j));
                    if (!result) {
                        return null;
                    }
                }
            }
        } catch (final Exception e) {
            PlotSquared.log("&7 - Cannot save: corrupt chunk at " + (i / 16) + ", " + (j / 16));
            return null;
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
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = 0; y < height; y++) {
                    final int index = (y * width * length) + (z * width) + x;
                    
                    Block block = worldObj.getBlockAt(sx + x, sy + y, sz + z);
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
                    int id = block.getTypeId();
                    byte data = block.getData();
                    if (id > 255) {
                        if (addBlocks == null) {
                            addBlocks = new byte[(blocks.length >> 1) + 1];
                        }
                        addBlocks[index >> 1] = (byte) (((index & 1) == 0) ? (addBlocks[index >> 1] & 0xF0) | ((id >> 8) & 0xF) : (addBlocks[index >> 1] & 0xF) | (((id >> 8) & 0xF) << 4));
                    }
                    blocks[index] = (byte) id;
                    blockData[index] = data;
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
        return new CompoundTag("Schematic", schematic);
    }

    
}
