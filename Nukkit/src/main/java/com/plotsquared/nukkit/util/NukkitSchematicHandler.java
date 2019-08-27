package com.plotsquared.nukkit.util;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
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
import com.intellectualcrafters.plot.util.block.LocalBlockQueue;
import com.plotsquared.nukkit.NukkitMain;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Schematic Handler.
 */
public class NukkitSchematicHandler extends SchematicHandler {

    private final NukkitMain plugin;

    public NukkitSchematicHandler(NukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void getCompoundTag(final String world, final Set<RegionWrapper> regions, final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                final Location bot = corners[0];
                Location top = corners[1];

                final int width = top.getX() - bot.getX() + 1;
                int height = top.getY() - bot.getY() + 1;
                final int length = top.getZ() - bot.getZ() + 1;
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
                final List<CompoundTag> tileEntities = new ArrayList<>();
                final byte[] blocks = new byte[width * height * length];
                final byte[] blockData = new byte[width * height * length];
                // Queue
                final ArrayDeque<RegionWrapper> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override
                    public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(() -> {
                                schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
                                schematic.put("Data", new ByteArrayTag("Data", blockData));
                                schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<>()));
                                schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
                                whenDone.value = new CompoundTag("Schematic", schematic);
                                TaskManager.runTask(whenDone);
                                System.gc();
                                System.gc();
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
                        final Level worldObj = plugin.getServer().getLevelByName(world);
                        // Main thread
                        final Vector3 mutable = new Vector3();
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                long start = System.currentTimeMillis();
                                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 20) {
                                    // save schematics
                                    ChunkLoc chunk = chunks.remove(0);
                                    BaseFullChunk bc = worldObj.getChunk(chunk.x, chunk.z);
                                    try {
                                        bc.load(false);
                                    } catch (IOException e) {
                                        continue;
                                    }
                                    int X = chunk.x;
                                    int Z = chunk.z;
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
                                        int ry = y - sy;
                                        int i1 = ry * width * length;
                                        for (int z = zzb; z <= zzt; z++) {
                                            int rz = z - bz;
                                            int i2 = i1 + rz * width;
                                            for (int x = xxb; x <= xxt; x++) {
                                                int rx = x - bx;
                                                int index = i2 + rx;
                                                mutable.x = x;
                                                mutable.y = y;
                                                mutable.z = z;
                                                Block block = worldObj.getBlock(mutable);
                                                blocks[index] = (byte) block.getId();
                                                blockData[index] = (byte) block.getDamage();
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
    public boolean restoreTile(LocalBlockQueue queue, CompoundTag ct, int x, int y, int z) {
        return false;
    }
}
