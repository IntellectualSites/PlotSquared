package com.plotsquared.sponge.util;

import com.intellectualcrafters.jnbt.ByteArrayTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.IntTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.StringTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SpongeSchematicHandler extends SchematicHandler {

    @Override
    public void restoreTile(String world, CompoundTag tag, int x, int y, int z) {
        // TODO Auto-generated method stub
        // This method should place the compound tag at a location e.g. chest contents
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void getCompoundTag(String world, Set<RegionWrapper> regions, RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                Location bot = corners[0];
                Location top = corners[1];

                int width = top.getX() - bot.getX() + 1;
                int height = top.getY() - bot.getY() + 1;
                int length = top.getZ() - bot.getZ() + 1;
                // Main Schematic tag
                HashMap<String, Tag> schematic = new HashMap<>();
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
                List<Tag> tileEntities = new ArrayList<>();
                byte[] blocks = new byte[width * height * length];
                byte[] blockData = new byte[width * height * length];
                // Queue
                ArrayDeque<RegionWrapper> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override
                    public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
                                    schematic.put("Data", new ByteArrayTag("Data", blockData));
                                    schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<>()));
                                    schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
                                    whenDone.value = new CompoundTag("Schematic", schematic);
                                    TaskManager.runTask(whenDone);
                                    System.gc();
                                    System.gc();
                                }
                            });
                            return;
                        }
                        Runnable regionTask = this;
                        RegionWrapper region = queue.poll();
                        Location pos1 = new Location(world, region.minX, region.minY, region.minZ);
                        Location pos2 = new Location(world, region.maxX, region.maxY, region.maxZ);
                        int bx = bot.getX();
                        int bz = bot.getZ();
                        int p1x = pos1.getX();
                        int p1z = pos1.getZ();
                        int p2x = pos2.getX();
                        int p2z = pos2.getZ();
                        int bcx = p1x >> 4;
                        int bcz = p1z >> 4;
                        int tcx = p2x >> 4;
                        int tcz = p2z >> 4;
                        int sy = pos1.getY();
                        int ey = pos2.getY();
                        // Generate list of chunks
                        ArrayList<ChunkLoc> chunks = new ArrayList<>();
                        for (int x = bcx; x <= tcx; x++) {
                            for (int z = bcz; z <= tcz; z++) {
                                chunks.add(new ChunkLoc(x, z));
                            }
                        }
                        World worldObj = SpongeUtil.getWorld(world);
                        // Main thread
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                long start = System.currentTimeMillis();
                                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 20) {
                                    // save schematics
                                    ChunkLoc chunk = chunks.remove(0);
                                    int X = chunk.x;
                                    int Z = chunk.z;
                                    int xxb = X << 4;
                                    int zzb = Z << 4;
                                    if (!worldObj.getChunk(xxb, 1, zzb).isPresent() && !worldObj.loadChunk(xxb, 1, zzb, false).isPresent()) {
                                        continue;
                                    }
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
                                        int ry = y - sy;
                                        int i1 = ry * width * length;
                                        for (int z = zzb; z <= zzt; z++) {
                                            int rz = z - p1z;
                                            int i2 = i1 + rz * width;
                                            for (int x = xxb; x <= xxt; x++) {
                                                int rx = x - p1x;
                                                int index = i2 + rx;

                                                BlockState state = worldObj.getBlock(x, y, z);
                                                PlotBlock block = SpongeUtil.getPlotBlock(state);
                                                int id = block.id;
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
                                                    case 192:
                                                        break;
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
                                                    case 178:
                                                        CompoundTag rawTag;
                                                        if (state instanceof Carrier) {
                                                            Carrier chest = (Carrier) state;
                                                            CarriedInventory<? extends Carrier> inv = chest.getInventory();
                                                            // TODO serialize inventory
                                                            rawTag = null;
                                                        } else {
                                                            rawTag = null;
                                                        }
                                                        if (rawTag != null) {
                                                            Map<String, Tag> values = new HashMap<>();
                                                            for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                                                values.put(entry.getKey(), entry.getValue());
                                                            }
                                                            values.put("id", new StringTag("id", "Chest"));
                                                            values.put("x", new IntTag("x", x));
                                                            values.put("y", new IntTag("y", y));
                                                            values.put("z", new IntTag("z", z));
                                                            CompoundTag tileEntityTag = new CompoundTag(values);
                                                            tileEntities.add(tileEntityTag);
                                                        }
                                                    default:
                                                        blockData[index] = block.data;
                                                }
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

}
