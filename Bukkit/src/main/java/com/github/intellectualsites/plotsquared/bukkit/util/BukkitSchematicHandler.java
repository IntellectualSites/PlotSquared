package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.object.schematic.StateWrapper;
import com.github.intellectualsites.plotsquared.plot.object.ChunkLoc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Schematic Handler.
 */
public abstract class BukkitSchematicHandler extends SchematicHandler {

    @Override public void getCompoundTag(final String world, final Set<RegionWrapper> regions,
        final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                final Location bot = corners[0];
                Location top = corners[1];

                CuboidRegion cuboidRegion =
                    new CuboidRegion(BukkitUtil.IMP.getWeWorld(world), bot.getBlockVector3(),
                        top.getBlockVector3());

                final int width = top.getX() - bot.getX() + 1;
                int height = top.getY() - bot.getY() + 1;
                final int length = top.getZ() - bot.getZ() + 1;
                Map<String, Tag> schematic = new HashMap<>();
                schematic.put("Version", new IntTag(1));

                Map<String, Tag> metadata = new HashMap<>();
                metadata.put("WEOffsetX", new IntTag(0));
                metadata.put("WEOffsetY", new IntTag(0));
                metadata.put("WEOffsetZ", new IntTag(0));

                schematic.put("Metadata", new CompoundTag(metadata));

                schematic.put("Width", new ShortTag((short) width));
                schematic.put("Height", new ShortTag((short) height));
                schematic.put("Length", new ShortTag((short) length));

                // The Sponge format Offset refers to the 'min' points location in the world. That's our 'Origin'
                schematic.put("Offset", new IntArrayTag(new int[] {0, 0, 0,}));

                final int[] paletteMax = {0};
                Map<String, Integer> palette = new HashMap<>();

                List<CompoundTag> tileEntities = new ArrayList<>();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);
                // Queue
                final ArrayDeque<RegionWrapper> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override public void run() {
                                    schematic.put("PaletteMax", new IntTag(paletteMax[0]));

                                    Map<String, Tag> paletteTag = new HashMap<>();
                                    palette.forEach(
                                        (key, value) -> paletteTag.put(key, new IntTag(value)));

                                    schematic.put("Palette", new CompoundTag(paletteTag));
                                    schematic
                                        .put("BlockData", new ByteArrayTag(buffer.toByteArray()));
                                    schematic.put("TileEntities",
                                        new ListTag(CompoundTag.class, tileEntities));
                                    whenDone.value = new CompoundTag(schematic);
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
                            @Override public void run() {
                                long start = System.currentTimeMillis();
                                while (!chunks.isEmpty()
                                    && System.currentTimeMillis() - start < 20) {
                                    // save schematics
                                    ChunkLoc chunk = chunks.remove(0);
                                    Chunk bc = worldObj.getChunkAt(chunk.x, chunk.z);
                                    if (!bc.load(false)) {
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
                                                BlockVector3 point = BlockVector3.at(x, y, z);
                                                BaseBlock block =
                                                    cuboidRegion.getWorld().getFullBlock(point);
                                                if (block.getNbtData() != null) {
                                                    Map<String, Tag> values = new HashMap<>();
                                                    for (Map.Entry<String, Tag> entry : block
                                                        .getNbtData().getValue().entrySet()) {
                                                        values
                                                            .put(entry.getKey(), entry.getValue());
                                                    }

                                                    values.remove(
                                                        "id"); // Remove 'id' if it exists. We want 'Id'

                                                    // Positions are kept in NBT, we don't want that.
                                                    values.remove("x");
                                                    values.remove("y");
                                                    values.remove("z");

                                                    values
                                                        .put("Id", new StringTag(block.getNbtId()));
                                                    values.put("Pos",
                                                        new IntArrayTag(new int[] {x, y, z}));

                                                    tileEntities.add(new CompoundTag(values));
                                                }
                                                String blockKey =
                                                    block.toImmutableState().getAsString();
                                                int blockId;
                                                if (palette.containsKey(blockKey)) {
                                                    blockId = palette.get(blockKey);
                                                } else {
                                                    blockId = paletteMax[0];
                                                    palette.put(blockKey, blockId);
                                                    paletteMax[0]++;
                                                }

                                                while ((blockId & -128) != 0) {
                                                    buffer.write(blockId & 127 | 128);
                                                    blockId >>>= 7;
                                                }
                                                buffer.write(blockId);
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
        return new StateWrapper(ct).restoreTag(queue.getWorld(), x, y, z);
    }
}
