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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.ClassicPlotWorld;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.MCEditSchematicReader;
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicHandler {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + SchematicHandler.class.getSimpleName());
    public static SchematicHandler manager;
    private final WorldUtil worldUtil;
    private boolean exportAll = false;

    public SchematicHandler(@Nonnull final WorldUtil worldUtil) {
        this.worldUtil = worldUtil;
    }

    public boolean exportAll(Collection<Plot> collection, final File outputDir,
        final String namingScheme, final Runnable ifSuccess) {
        if (this.exportAll) {
            return false;
        }
        if (collection.isEmpty()) {
            return false;
        }
        this.exportAll = true;
        final ArrayList<Plot> plots = new ArrayList<>(collection);
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                if (plots.isEmpty()) {
                    SchematicHandler.this.exportAll = false;
                    TaskManager.runTask(ifSuccess);
                    return;
                }
                Iterator<Plot> i = plots.iterator();
                final Plot plot = i.next();
                i.remove();

                final String owner;
                if (plot.hasOwner()) {
                    owner = plot.getOwnerAbs().toString();
                } else {
                    owner = "unknown";
                }

                final String name;
                if (namingScheme == null) {
                    name =
                        plot.getId().x + ";" + plot.getId().y + ',' + plot.getArea() + ',' + owner;
                } else {
                    name = namingScheme.replaceAll("%id%", plot.getId().toString())
                        .replaceAll("%idx%", plot.getId().x + "")
                        .replaceAll("%idy%", plot.getId().y + "")
                        .replaceAll("%world%", plot.getArea().toString());
                }

                final String directory;
                if (outputDir == null) {
                    directory = Settings.Paths.SCHEMATICS;
                } else {
                    directory = outputDir.getAbsolutePath();
                }

                final Runnable THIS = this;
                getCompoundTag(plot, new RunnableVal<CompoundTag>() {
                    @Override public void run(final CompoundTag value) {
                        if (value != null) {
                            TaskManager.runTaskAsync(() -> {
                                boolean result =
                                    save(value, directory + File.separator + name + ".schem");
                                if (!result) {
                                    logger.error("[P2] Failed to save {}", plot.getId());
                                }
                                TaskManager.runTask(THIS);
                            });
                        }
                    }
                });
            }
        });
        return true;
    }

    /**
     * Paste a schematic.
     *
     * @param schematic the schematic object to paste
     * @param plot      plot to paste in
     * @param xOffset   offset x to paste it from plot origin
     * @param zOffset   offset z to paste it from plot origin
     */
    public void paste(final Schematic schematic, final Plot plot, final int xOffset,
        final int yOffset, final int zOffset, final boolean autoHeight,
        final RunnableVal<Boolean> whenDone) {

        TaskManager.runTask(() -> {
            if (whenDone != null) {
                whenDone.value = false;
            }
            if (schematic == null) {
                TaskManager.runTask(whenDone);
                return;
            }
            try {
                final QueueCoordinator queue = plot.getArea().getQueue();
                BlockVector3 dimension = schematic.getClipboard().getDimensions();
                final int WIDTH = dimension.getX();
                final int LENGTH = dimension.getZ();
                final int HEIGHT = dimension.getY();
                // Validate dimensions
                CuboidRegion region = plot.getLargestRegion();
                if (((region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + xOffset
                    + 1) < WIDTH) || (
                    (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + zOffset
                        + 1) < LENGTH) || (HEIGHT > 256)) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                // block type and data arrays
                final Clipboard blockArrayClipboard = schematic.getClipboard();
                // Calculate the optimal height to paste the schematic at
                final int y_offset_actual;
                if (autoHeight) {
                    if (HEIGHT >= 256) {
                        y_offset_actual = yOffset;
                    } else {
                        PlotArea pw = plot.getArea();
                        if (pw instanceof ClassicPlotWorld) {
                            y_offset_actual = yOffset + ((ClassicPlotWorld) pw).PLOT_HEIGHT;
                        } else {
                            y_offset_actual = yOffset + 1 + this.worldUtil
                                .getHighestBlockSynchronous(plot.getWorldName(),
                                    region.getMinimumPoint().getX() + 1,
                                    region.getMinimumPoint().getZ() + 1);
                        }
                    }
                } else {
                    y_offset_actual = yOffset;
                }

                final Location pos1 = Location
                    .at(plot.getWorldName(), region.getMinimumPoint().getX() + xOffset,
                        y_offset_actual, region.getMinimumPoint().getZ() + zOffset);
                final Location pos2 = pos1.add(WIDTH - 1, HEIGHT - 1, LENGTH - 1);

                final int p1x = pos1.getX();
                final int p1z = pos1.getZ();
                final int p2x = pos2.getX();
                final int p2z = pos2.getZ();
                final int bcx = p1x >> 4;
                final int bcz = p1z >> 4;
                final int tcx = p2x >> 4;
                final int tcz = p2z >> 4;

                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override public void run(int[] value) {
                        BlockVector2 chunk = BlockVector2.at(value[0], value[1]);
                        int x = chunk.getX();
                        int z = chunk.getZ();
                        int xxb = x << 4;
                        int zzb = z << 4;
                        int xxt = xxb + 15;
                        int zzt = zzb + 15;
                        if (x == bcx) {
                            xxb = p1x;
                        }
                        if (x == tcx) {
                            xxt = p2x;
                        }
                        if (z == bcz) {
                            zzb = p1z;
                        }
                        if (z == tcz) {
                            zzt = p2z;
                        }
                        // Paste schematic here

                        for (int ry = 0; ry < Math.min(256, HEIGHT); ry++) {
                            int yy = y_offset_actual + ry;
                            if (yy > 255) {
                                continue;
                            }
                            for (int rz = zzb - p1z; rz <= (zzt - p1z); rz++) {
                                for (int rx = xxb - p1x; rx <= (xxt - p1x); rx++) {
                                    int xx = p1x + xOffset + rx;
                                    int zz = p1z + zOffset + rz;
                                    BaseBlock id = blockArrayClipboard
                                        .getFullBlock(BlockVector3.at(rx, ry, rz));
                                    queue.setBlock(xx, yy, zz, id);
                                    if (ry == 0) {
                                        BiomeType biome =
                                            blockArrayClipboard.getBiome(BlockVector2.at(rx, rz));
                                        queue.setBiome(xx, zz, biome);
                                    }
                                }
                            }
                        }
                        queue.enqueue();
                    }
                }, () -> {
                    if (whenDone != null) {
                        whenDone.value = true;
                        whenDone.run();
                    }
                }, 10);
            } catch (Exception e) {
                e.printStackTrace();
                TaskManager.runTask(whenDone);
            }
        });
    }

    public abstract boolean restoreTile(QueueCoordinator queue, CompoundTag tag, int x, int y,
        int z);

    /**
     * Get a schematic
     *
     * @param name to check
     * @return schematic if found, else null
     */
    public Schematic getSchematic(String name) throws UnsupportedFormatException {
        File parent =
            MainUtil.getFile(PlotSquared.platform().getDirectory(), Settings.Paths.SCHEMATICS);
        if (!parent.exists()) {
            if (!parent.mkdir()) {
                throw new RuntimeException("Could not create schematic parent directory");
            }
        }
        if (!name.endsWith(".schem") && !name.endsWith(".schematic")) {
            name = name + ".schem";
        }
        File file = MainUtil.getFile(PlotSquared.platform().getDirectory(),
            Settings.Paths.SCHEMATICS + File.separator + name);
        if (!file.exists()) {
            file = MainUtil.getFile(PlotSquared.platform().getDirectory(),
                Settings.Paths.SCHEMATICS + File.separator + name);
        }
        return getSchematic(file);
    }

    /**
     * Get an immutable collection containing all schematic names
     *
     * @return Immutable collection with schematic names
     */
    public Collection<String> getSchematicNames() {
        final File parent =
            MainUtil.getFile(PlotSquared.platform().getDirectory(), Settings.Paths.SCHEMATICS);
        final List<String> names = new ArrayList<>();
        if (parent.exists()) {
            final String[] rawNames =
                parent.list((dir, name) -> name.endsWith(".schematic") || name.endsWith(".schem"));
            if (rawNames != null) {
                final List<String> transformed = Arrays.stream(rawNames)
                    //.map(rawName -> rawName.substring(0, rawName.length() - 10))
                    .collect(Collectors.toList());
                names.addAll(transformed);
            }
        }
        return Collections.unmodifiableList(names);
    }

    /**
     * Get a schematic
     *
     * @param file to check
     * @return schematic if found, else null
     */
    public Schematic getSchematic(File file) throws UnsupportedFormatException {
        if (!file.exists()) {
            return null;
        }
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format != null) {
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                Clipboard clip = reader.read();
                return new Schematic(clip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new UnsupportedFormatException(
                "This schematic format is not recognised or supported.");
        }
        return null;
    }

    public Schematic getSchematic(@Nonnull URL url) {
        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            InputStream inputStream = Channels.newInputStream(readableByteChannel);
            return getSchematic(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Schematic getSchematic(@Nonnull InputStream is) {
        try {
            SpongeSchematicReader schematicReader =
                new SpongeSchematicReader(new NBTInputStream(new GZIPInputStream(is)));
            Clipboard clip = schematicReader.read();
            return new Schematic(clip);
        } catch (IOException ignored) {
            try {
                MCEditSchematicReader schematicReader =
                    new MCEditSchematicReader(new NBTInputStream(new GZIPInputStream(is)));
                Clipboard clip = schematicReader.read();
                return new Schematic(clip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<String> getSaves(UUID uuid) {
        String rawJSON = "";
        try {
            String website = Settings.Web.URL + "list.php?" + uuid.toString();
            URL url = new URL(website);
            URLConnection connection = new URL(url.toString()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
                rawJSON = reader.lines().collect(Collectors.joining());
            }
            JSONArray array = new JSONArray(rawJSON);
            List<String> schematics = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String schematic = array.getString(i);
                schematics.add(schematic);
            }
            return schematics;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void upload(final CompoundTag tag, UUID uuid, String file, RunnableVal<URL> whenDone) {
        if (tag == null) {
            TaskManager.runTask(whenDone);
            return;
        }
        MainUtil.upload(uuid, file, "schem", new RunnableVal<OutputStream>() {
            @Override public void run(OutputStream output) {
                try (NBTOutputStream nos = new NBTOutputStream(
                    new GZIPOutputStream(output, true))) {
                    nos.writeNamedTag("Schematic", tag);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }, whenDone);
    }

    /**
     * Saves a schematic to a file path.
     *
     * @param tag  to save
     * @param path to save in
     * @return true if succeeded
     */
    public boolean save(CompoundTag tag, String path) {
        if (tag == null) {
            return false;
        }
        try {
            File tmp = MainUtil.getFile(PlotSquared.platform().getDirectory(), path);
            tmp.getParentFile().mkdirs();
            try (NBTOutputStream nbtStream = new NBTOutputStream(
                new GZIPOutputStream(new FileOutputStream(tmp)))) {
                nbtStream.writeNamedTag("Schematic", tag);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void getCompoundTag(final String world, final Set<CuboidRegion> regions,
        final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(() -> {
            // Main positions
            Location[] corners = MainUtil.getCorners(world, regions);
            final Location bot = corners[0];
            final Location top = corners[1];

            CuboidRegion cuboidRegion =
                new CuboidRegion(this.worldUtil.getWeWorld(world), bot.getBlockVector3(),
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
                            palette.forEach((key, value) -> paletteTag.put(key, new IntTag(value)));

                            schematic.put("Palette", new CompoundTag(paletteTag));
                            schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
                            schematic
                                .put("TileEntities", new ListTag(CompoundTag.class, tileEntities));

                            schematic.put("BiomePaletteMax", new IntTag(biomePalette.size()));

                            Map<String, Tag> biomePaletteTag = new HashMap<>();
                            biomePalette.forEach(
                                (key, value) -> biomePaletteTag.put(key, new IntTag(value)));

                            schematic.put("BiomePalette", new CompoundTag(biomePaletteTag));
                            schematic.put("BiomeData", new ByteArrayTag(biomeBuffer.toByteArray()));
                            whenDone.value = new CompoundTag(schematic);
                            TaskManager.runTask(whenDone);
                        });
                        return;
                    }
                    final Runnable regionTask = this;
                    CuboidRegion region = queue.poll();

                    final Location pos1 = Location.at(world, region.getMinimumPoint());
                    final Location pos2 = Location.at(world, region.getMaximumPoint());

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
                            while (yiter.hasNext() && System.currentTimeMillis() - ystart < 20) {
                                final int y = yiter.next();
                                Iterator<Integer> ziter = IntStream.range(p1z, p2z + 1).iterator();
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
                                                        BaseBlock block = cuboidRegion.getWorld()
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

                                                            values.put("Id",
                                                                new StringTag(block.getNbtId()));
                                                            values.put("Pos", new IntArrayTag(
                                                                new int[] {rx, ry, rz}));

                                                            tileEntities
                                                                .add(new CompoundTag(values));
                                                        }
                                                        String blockKey =
                                                            block.toImmutableState().getAsString();
                                                        int blockId;
                                                        if (palette.containsKey(blockKey)) {
                                                            blockId = palette.get(blockKey);
                                                        } else {
                                                            blockId = palette.size();
                                                            palette.put(blockKey, palette.size());
                                                        }

                                                        while ((blockId & -128) != 0) {
                                                            buffer.write(blockId & 127 | 128);
                                                            blockId >>>= 7;
                                                        }
                                                        buffer.write(blockId);

                                                        if (ry > 0) {
                                                            continue;
                                                        }
                                                        BlockVector2 pt = BlockVector2.at(x, z);
                                                        BiomeType biome =
                                                            cuboidRegion.getWorld().getBiome(pt);
                                                        String biomeStr = biome.getId();
                                                        int biomeId;
                                                        if (biomePalette.containsKey(biomeStr)) {
                                                            biomeId = biomePalette.get(biomeStr);
                                                        } else {
                                                            biomeId = biomePalette.size();
                                                            biomePalette.put(biomeStr, biomeId);
                                                        }
                                                        while ((biomeId & -128) != 0) {
                                                            biomeBuffer.write(biomeId & 127 | 128);
                                                            biomeId >>>= 7;
                                                        }
                                                        biomeBuffer.write(biomeId);
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
                                TaskManager.runTaskLater(this, TaskTime.ticks(1L));
                            } else {
                                regionTask.run();
                            }
                        }
                    };
                    yTask.run();
                }
            });
        });
    }

    public void getCompoundTag(final Plot plot, final RunnableVal<CompoundTag> whenDone) {
        getCompoundTag(plot.getWorldName(), plot.getRegions(), new RunnableVal<CompoundTag>() {
            @Override public void run(CompoundTag value) {
                whenDone.run(value);
            }
        });
    }


    public static class UnsupportedFormatException extends Exception {
        /**
         * Throw with a message.
         *
         * @param message the message
         */
        public UnsupportedFormatException(String message) {
            super(message);
        }

        /**
         * Throw with a message and a cause.
         *
         * @param message the message
         * @param cause   the cause
         */
        public UnsupportedFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
