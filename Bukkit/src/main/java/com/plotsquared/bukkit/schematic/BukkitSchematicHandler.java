/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.schematic;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Schematic Handler.
 */
public class BukkitSchematicHandler extends SchematicHandler {

    @Override
    public void getCompoundTag(final String world, final Set<CuboidRegion> regions,
        final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                final Location bot = corners[0];
                final Location top = corners[1];

                CuboidRegion cuboidRegion =
                    new CuboidRegion(BukkitUtil.IMP.getWeWorld(world), bot.getBlockVector3(),
                        top.getBlockVector3());

                final int width = cuboidRegion.getWidth();
                int height = cuboidRegion.getHeight();
                final int length = cuboidRegion.getLength();
                Map<String, Tag> schematic = new HashMap<>();
                schematic.put("Version", new IntTag(2));
                schematic.put("DataVersion", new IntTag(WorldEdit.getInstance().getPlatformManager()
                    .queryCapability(Capability.WORLD_EDITING).getDataVersion()));

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

                Map<String, Integer> palette = new HashMap<>();
                Map<String, Integer> biomePalette = new HashMap<>();

                List<CompoundTag> tileEntities = new ArrayList<>();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);
                ByteArrayOutputStream biomeBuffer = new ByteArrayOutputStream(width * length);
                // Queue
                final ArrayDeque<CuboidRegion> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(() -> {
                                schematic.put("PaletteMax", new IntTag(palette.size()));

                                Map<String, Tag> paletteTag = new HashMap<>();
                                palette.forEach(
                                    (key, value) -> paletteTag.put(key, new IntTag(value)));

                                schematic.put("Palette", new CompoundTag(paletteTag));
                                schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
                                schematic.put("TileEntities",
                                    new ListTag(CompoundTag.class, tileEntities));

                                schematic.put("BiomePaletteMax", new IntTag(biomePalette.size()));

                                Map<String, Tag> biomePaletteTag = new HashMap<>();
                                biomePalette.forEach(
                                    (key, value) -> biomePaletteTag.put(key, new IntTag(value)));

                                schematic.put("BiomePalette", new CompoundTag(biomePaletteTag));
                                schematic
                                    .put("BiomeData", new ByteArrayTag(biomeBuffer.toByteArray()));
                                whenDone.value = new CompoundTag(schematic);
                                TaskManager.runTask(whenDone);
                            });
                            return;
                        }
                        final Runnable regionTask = this;
                        CuboidRegion region = queue.poll();
                        Location pos1 = new Location(world, region.getMinimumPoint().getX(),
                            region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
                        Location pos2 = new Location(world, region.getMaximumPoint().getX(),
                            region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());
                        final int p1x = pos1.getX();
                        final int sy = pos1.getY();
                        final int p1z = pos1.getZ();
                        final int p2x = pos2.getX();
                        final int p2z = pos2.getZ();
                        final int ey = pos2.getY();
                        Iterator<Integer> yiter = IntStream.range(sy, ey + 1).iterator();
                        final Runnable yTask = new Runnable() {
                            @Override public void run() {
                                long ystart = System.currentTimeMillis();
                                while (yiter.hasNext()
                                    && System.currentTimeMillis() - ystart < 20) {
                                    final int y = yiter.next();
                                    Iterator<Integer> ziter =
                                        IntStream.range(p1z, p2z + 1).iterator();
                                    final Runnable zTask = new Runnable() {
                                        @Override public void run() {
                                            long zstart = System.currentTimeMillis();
                                            while (ziter.hasNext()
                                                && System.currentTimeMillis() - zstart < 20) {
                                                final int z = ziter.next();
                                                Iterator<Integer> xiter =
                                                    IntStream.range(p1x, p2x + 1).iterator();
                                                final Runnable xTask = new Runnable() {
                                                    @Override public void run() {
                                                        long xstart = System.currentTimeMillis();
                                                        final int ry = y - sy;
                                                        final int rz = z - p1z;
                                                        while (xiter.hasNext()
                                                            && System.currentTimeMillis() - xstart
                                                            < 20) {
                                                            final int x = xiter.next();
                                                            final int rx = x - p1x;
                                                            BlockVector3 point =
                                                                BlockVector3.at(x, y, z);
                                                            BaseBlock block =
                                                                cuboidRegion.getWorld()
                                                                    .getFullBlock(point);
                                                            if (block.getNbtData() != null) {
                                                                Map<String, Tag> values =
                                                                    new HashMap<>();
                                                                for (Map.Entry<String, Tag> entry : block
                                                                    .getNbtData().getValue()
                                                                    .entrySet()) {
                                                                    values.put(entry.getKey(),
                                                                        entry.getValue());
                                                                }
                                                                // Remove 'id' if it exists. We want 'Id'
                                                                values.remove("id");

                                                                // Positions are kept in NBT, we don't want that.
                                                                values.remove("x");
                                                                values.remove("y");
                                                                values.remove("z");

                                                                values.put("Id", new StringTag(
                                                                    block.getNbtId()));
                                                                values.put("Pos", new IntArrayTag(
                                                                    new int[] {rx, ry, rz}));

                                                                tileEntities
                                                                    .add(new CompoundTag(values));
                                                            }
                                                            String blockKey =
                                                                block.toImmutableState()
                                                                    .getAsString();
                                                            int blockId;
                                                            if (palette.containsKey(blockKey)) {
                                                                blockId = palette.get(blockKey);
                                                            } else {
                                                                blockId = palette.size();
                                                                palette
                                                                    .put(blockKey, palette.size());
                                                            }

                                                            while ((blockId & -128) != 0) {
                                                                buffer.write(blockId & 127 | 128);
                                                                blockId >>>= 7;
                                                            }
                                                            buffer.write(blockId);

                                                            if (ry == sy) {
                                                                BlockVector2 pt =
                                                                    BlockVector2.at(x, z);
                                                                BiomeType biome =
                                                                    cuboidRegion.getWorld()
                                                                        .getBiome(pt);
                                                                String biomeStr = biome.getId();
                                                                int biomeId;
                                                                if (biomePalette
                                                                    .containsKey(biomeStr)) {
                                                                    biomeId =
                                                                        biomePalette.get(biomeStr);
                                                                } else {
                                                                    biomeId = biomePalette.size();
                                                                    biomePalette
                                                                        .put(biomeStr, biomeId);
                                                                }
                                                                while ((biomeId & -128) != 0) {
                                                                    biomeBuffer
                                                                        .write(biomeId & 127 | 128);
                                                                    biomeId >>>= 7;
                                                                }
                                                                biomeBuffer.write(biomeId);
                                                            }
                                                        }
                                                        if (xiter.hasNext()) {
                                                            this.run();
                                                        }
                                                    }
                                                };
                                                xTask.run();
                                            }
                                            if (ziter.hasNext()) {
                                                this.run();
                                            }
                                        }
                                    };
                                    zTask.run();
                                }
                                if (yiter.hasNext()) {
                                    TaskManager.runTaskLater(this, 1);
                                } else {
                                    regionTask.run();
                                }
                            }
                        };
                        yTask.run();
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
