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
package com.plotsquared.bukkit.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.object.schematic.StateWrapper;

/**
 * Schematic Handler
 *


 */
public class BukkitSchematicHandler extends SchematicHandler {

    @Override
    public void getCompoundTag(final String world, final Set<RegionWrapper> regions, final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                final Location bot = corners[0];
                final Location top = corners[1];

                final int width = (top.getX() - bot.getX()) + 1;
                final int height = (top.getY() - bot.getY()) + 1;
                final int length = (top.getZ() - bot.getZ()) + 1;
                // Main Schematic tag
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
                // Arrays of data types
                final List<Tag> tileEntities = new ArrayList<>();
                final byte[] blocks = new byte[width * height * length];
                final byte[] blockData = new byte[width * height * length];
                // Queue
                final ArrayDeque<RegionWrapper> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override
                    public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
                                    schematic.put("Data", new ByteArrayTag("Data", blockData));
                                    schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
                                    schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
                                    whenDone.value = new CompoundTag("Schematic", schematic);
                                    TaskManager.runTask(whenDone);
                                    System.gc();
                                    System.gc();
                                }
                            });
                            return;
                        }
                        final Runnable regionTask = this;
                        RegionWrapper region = queue.poll();
                        Location pos1 = new Location(world, region.minX, region.minY, region.minZ);
                        Location pos2 = new Location(world, region.maxX, region.maxY, region.maxZ);
                        final int bx = bot.getX();
                        final int bz = bot.getZ();
                        final int p1x = pos1.getX();
                        final int p1z = pos1.getZ();
                        final int p2x = pos2.getX();
                        final int p2z = pos2.getZ();
                        final int bcx = p1x >> 4;
                        final int bcz = p1z >> 4;
                        final int tcx = p2x >> 4;
                        final int tcz = p2z >> 4;
                        final int sy = pos1.getY();
                        final int ey = pos2.getY();
                        // Generate list of chunks
                        final ArrayList<ChunkLoc> chunks = new ArrayList<>();
                        for (int x = bcx; x <= tcx; x++) {
                            for (int z = bcz; z <= tcz; z++) {
                                chunks.add(new ChunkLoc(x, z));
                            }
                        }
                        final World worldObj = Bukkit.getWorld(world);
                        // Main thread
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                final long start = System.currentTimeMillis();
                                while ((!chunks.isEmpty()) && ((System.currentTimeMillis() - start) < 20)) {
                                    // save schematics
                                    final ChunkLoc chunk = chunks.remove(0);
                                    final Chunk bc = worldObj.getChunkAt(chunk.x, chunk.z);
                                    if (!bc.load(false)) {
                                        continue;
                                    }
                                    final int X = chunk.x;
                                    final int Z = chunk.z;
                                    int xxb = X << 4;
                                    int zzb = Z << 4;
                                    int xxt = xxb + 15;
                                    int zzt = zzb + 15;

                                    if (X == bcx) {
                                        xxb = p1x;
                                    }
                                    if (X == tcx) {
                                        xxt = p2x;
                                    }
                                    if (Z == bcz) {
                                        zzb = p1z;
                                    }
                                    if (Z == tcz) {
                                        zzt = p2z;
                                    }
                                    for (int y = sy; y <= Math.min(255, ey); y++) {
                                        final int ry = y - sy;
                                        final int i1 = (ry * width * length);
                                        for (int z = zzb; z <= zzt; z++) {
                                            final int rz = z - bz;
                                            final int i2 = i1 + (rz * width);
                                            for (int x = xxb; x <= xxt; x++) {
                                                final int rx = x - bx;
                                                final int index = i2 + rx;
                                                final Block block = worldObj.getBlockAt(x, y, z);
                                                final int id = block.getTypeId();
                                                switch (id) {
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
                                                        final BlockState state = block.getState();
                                                        if (state != null) {
                                                            final StateWrapper wrapper = new StateWrapper(state);
                                                            final CompoundTag rawTag = wrapper.getTag();
                                                            if (rawTag != null) {
                                                                final Map<String, Tag> values = new HashMap<String, Tag>();
                                                                for (final Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                                                    values.put(entry.getKey(), entry.getValue());
                                                                }
                                                                values.put("id", new StringTag("id", wrapper.getId()));
                                                                values.put("x", new IntTag("x", x));
                                                                values.put("y", new IntTag("y", y));
                                                                values.put("z", new IntTag("z", z));
                                                                final CompoundTag tileEntityTag = new CompoundTag(values);
                                                                tileEntities.add(tileEntityTag);
                                                            }
                                                        }
                                                    }
                                                    default: {
                                                        blockData[index] = block.getData();
                                                    }
                                                }
                                                // For optimization reasons, we are not supporting custom data types
                                                // Especially since the most likely reason beyond  this range is modded servers in which the blocks
                                                // have NBT
                                                //                                        if (type > 255) {
                                                //                                            if (addBlocks == null) {
                                                //                                                addBlocks = new byte[(blocks.length >> 1) + 1];
                                                //                                            }
                                                //                                            addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                                // (addBlocks[index >> 1] & 0xF0) | ((type >> 8) & 0xF) : (addBlocks[index >> 1] & 0xF) | (((type
                                                // >> 8) & 0xF) << 4));
                                                //                                        }
                                                blocks[index] = (byte) id;
                                            }
                                        }
                                    }
                                }
                                if (!chunks.isEmpty()) {
                                    TaskManager.runTaskLater(this, 1);
                                } else {
                                    regionTask.run();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void restoreTag(CompoundTag ct, short x, short y, short z, Schematic schem) {
        new StateWrapper(ct).restoreTag(x, y, z, schem);
    }
}
