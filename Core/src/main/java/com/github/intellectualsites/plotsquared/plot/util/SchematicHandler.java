package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.jnbt.*;
import com.github.intellectualsites.plotsquared.json.JSONArray;
import com.github.intellectualsites.plotsquared.json.JSONException;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.generator.ClassicPlotWorld;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicHandler {
    public static SchematicHandler manager;

    private boolean exportAll = false;

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
                String o = UUIDHandler.getName(plot.owner);
                if (o == null) {
                    o = "unknown";
                }
                final String name;
                if (namingScheme == null) {
                    name = plot.getId().x + ";" + plot.getId().y + ',' + plot.getArea() + ',' + o;
                } else {
                    name = namingScheme.replaceAll("%owner%", o)
                        .replaceAll("%id%", plot.getId().toString())
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
                SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
                    @Override public void run(final CompoundTag value) {
                        if (value == null) {
                            MainUtil.sendMessage(null, "&7 - Skipped plot &c" + plot.getId());
                        } else {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override public void run() {
                                    MainUtil.sendMessage(null, "&6ID: " + plot.getId());
                                    boolean result = SchematicHandler.manager.save(value,
                                        directory + File.separator + name + ".schematic");
                                    if (!result) {
                                        MainUtil.sendMessage(null,
                                            "&7 - Failed to save &c" + plot.getId());
                                    } else {
                                        MainUtil
                                            .sendMessage(null, "&7 - &a  success: " + plot.getId());
                                    }
                                    TaskManager.runTask(new Runnable() {
                                        @Override public void run() {
                                            THIS.run();
                                        }
                                    });
                                }
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
     * @return boolean true if succeeded
     */
    public void paste(final Schematic schematic, final Plot plot, final int xOffset,
        final int yOffset, final int zOffset, final boolean autoHeight,
        final RunnableVal<Boolean> whenDone) {
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                if (whenDone != null) {
                    whenDone.value = false;
                }
                if (schematic == null) {
                    PlotSquared.debug("Schematic == null :|");
                    TaskManager.runTask(whenDone);
                    return;
                }
                try {
                    // Set flags
                    if (plot.hasOwner()) {
                        Map<String, Tag> flags = schematic.getFlags();
                        if (!flags.isEmpty()) {
                            for (Map.Entry<String, Tag> entry : flags.entrySet()) {
                                //plot.setFlag(entry.getKey(), StringTag.class.cast(entry.getValue()).getValue());
                            }

                        }
                    }
                    final LocalBlockQueue queue = plot.getArea().getQueue(false);
                    Dimension dimension = schematic.getSchematicDimension();
                    final int WIDTH = dimension.getX();
                    final int LENGTH = dimension.getZ();
                    final int HEIGHT = dimension.getY();
                    // Validate dimensions
                    RegionWrapper region = plot.getLargestRegion();
                    if (((region.maxX - region.minX + xOffset + 1) < WIDTH) || (
                        (region.maxZ - region.minZ + zOffset + 1) < LENGTH) || (HEIGHT > 256)) {
                        PlotSquared.debug("Schematic is too large");
                        PlotSquared.debug(
                            "(" + WIDTH + ',' + LENGTH + ',' + HEIGHT + ") is bigger than (" + (
                                region.maxX - region.minX) + ',' + (region.maxZ - region.minZ)
                                + ",256)");
                        TaskManager.runTask(whenDone);
                        return;
                    }
                    // block type and data arrays
                    final short[] ids = schematic.ids;
                    final byte[] datas = schematic.datas;
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
                                y_offset_actual = yOffset + 1 + MainUtil
                                    .getHeighestBlock(plot.getWorldName(), region.minX + 1,
                                        region.minZ + 1);
                            }
                        }
                    } else {
                        y_offset_actual = yOffset;
                    }
                    Location pos1 =
                        new Location(plot.getWorldName(), region.minX + xOffset, y_offset_actual,
                            region.minZ + zOffset);
                    Location pos2 = pos1.clone().add(WIDTH - 1, HEIGHT - 1, LENGTH - 1);
                    // TODO switch to ChunkManager.chunkTask(pos1, pos2, task, whenDone, allocate);
                    final int p1x = pos1.getX();
                    final int p1z = pos1.getZ();
                    final int p2x = pos2.getX();
                    final int p2z = pos2.getZ();
                    final int bcx = p1x >> 4;
                    final int bcz = p1z >> 4;
                    final int tcx = p2x >> 4;
                    final int tcz = p2z >> 4;
                    final ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
                    for (int x = bcx; x <= tcx; x++) {
                        for (int z = bcz; z <= tcz; z++) {
                            chunks.add(new ChunkLoc(x, z));
                        }
                    }
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override public void run() {
                            int count = 0;
                            while (!chunks.isEmpty() && count < 256) {
                                count++;
                                ChunkLoc chunk = chunks.remove(0);
                                int x = chunk.x;
                                int z = chunk.z;
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
                                    int i1 = ry * WIDTH * LENGTH;
                                    for (int rz = zzb - p1z; rz <= (zzt - p1z); rz++) {
                                        int i2 = (rz * WIDTH) + i1;
                                        for (int rx = xxb - p1x; rx <= (xxt - p1x); rx++) {
                                            int i = i2 + rx;
                                            int xx = p1x + rx;
                                            int zz = p1z + rz;
                                            int id = ids[i];
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
                                                    queue.setBlock(xx, yy, zz, id);
                                                    break;
                                                default:
                                                    queue.setBlock(xx, yy, zz,
                                                        PlotBlock.get((short) id, datas[i]));
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!chunks.isEmpty()) {
                                this.run();
                            } else {
                                queue.flush();
                                HashMap<BlockLoc, CompoundTag> tiles = schematic.getTiles();
                                if (!tiles.isEmpty()) {
                                    TaskManager.IMP.sync(new RunnableVal<Object>() {
                                        @Override public void run(Object value) {
                                            for (Map.Entry<BlockLoc, CompoundTag> entry : schematic
                                                .getTiles().entrySet()) {
                                                BlockLoc loc = entry.getKey();
                                                restoreTile(queue, entry.getValue(),
                                                    p1x + xOffset + loc.x, loc.y + y_offset_actual,
                                                    p1z + zOffset + loc.z);
                                            }
                                        }
                                    });
                                }
                                if (whenDone != null) {
                                    whenDone.value = true;
                                    whenDone.run();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }

    public Schematic getSchematic(CompoundTag tag) {
        Map<String, Tag> tagMap = tag.getValue();
        byte[] addBlocks = null;
        if (tagMap.containsKey("AddBlocks")) {
            addBlocks = ByteArrayTag.class.cast(tagMap.get("AddBlocks")).getValue();
        }

        short width = ShortTag.class.cast(tagMap.get("Width")).getValue();
        short length = ShortTag.class.cast(tagMap.get("Length")).getValue();
        short height = ShortTag.class.cast(tagMap.get("Height")).getValue();
        byte[] block_sml = ByteArrayTag.class.cast(tagMap.get("Blocks")).getValue();
        byte[] data = ByteArrayTag.class.cast(tagMap.get("Data")).getValue();
        Map<String, Tag> flags;
        if (tagMap.containsKey("Flags")) {
            flags = CompoundTag.class.cast(tagMap.get("Flags")).getValue();
        } else {
            flags = null;
        }

        short[] block = new short[block_sml.length];
        for (int i = 0; i < block.length; i++) {
            short id = block_sml[i];
            if (id < 0) {
                id = (short) (id & 0xFF);
            }
            block[i] = id;
        }

        if (addBlocks != null) {
            if (addBlocks.length == block.length) {
                for (int i = 0; i < addBlocks.length; i++) {
                    byte val = addBlocks[i];
                    if (val != 0) {
                        block[i] |= (val << 8);
                    }
                }
            } else {
                for (int index = 0; index < block.length; index++) {
                    if ((index & 1) == 0) {
                        block[index] =
                            (short) (((addBlocks[index >> 1] & 0x0F) << 8) + (block[index]));
                    } else {
                        block[index] =
                            (short) (((addBlocks[index >> 1] & 0xF0) << 4) + (block[index]));
                    }
                }
            }
        }

        // Slow as wrapper for each block
        //        final DataCollection[] collection = new DataCollection[b.length];
        //        for (int x = 0; x < b.length; x++) {
        //            collection[x] = new DataCollection(blocks[x], d[x]);
        //        }
        //        Schematic schem = new Schematic(collection, dimension, file);

        Dimension dimensions = new Dimension(width, height, length);
        Schematic schem = new Schematic(block, data, dimensions, flags);
        // Slow
        try {
            List<Tag> blockStates = ListTag.class.cast(tagMap.get("TileEntities")).getValue();
            for (Tag stateTag : blockStates) {
                try {
                    CompoundTag ct = (CompoundTag) stateTag;
                    Map<String, Tag> state = ct.getValue();
                    short x = IntTag.class.cast(state.get("x")).getValue().shortValue();
                    short y = IntTag.class.cast(state.get("y")).getValue().shortValue();
                    short z = IntTag.class.cast(state.get("z")).getValue().shortValue();
                    schem.addTile(new BlockLoc(x, y, z), ct);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schem;
    }

    public abstract boolean restoreTile(LocalBlockQueue queue, CompoundTag tag, int x, int y,
        int z);

    /**
     * Get a schematic
     *
     * @param name to check
     * @return schematic if found, else null
     */
    public Schematic getSchematic(String name) {
        File parent =
            MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), Settings.Paths.SCHEMATICS);
        if (!parent.exists()) {
            if (!parent.mkdir()) {
                throw new RuntimeException("Could not create schematic parent directory");
            }
        }
        File file = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(),
            Settings.Paths.SCHEMATICS + File.separator + name + (name.endsWith(".schematic") ?
                "" :
                ".schematic"));
        return getSchematic(file);
    }

    /**
     * Get an immutable collection containing all schematic names
     *
     * @return Immutable collection with schematic names
     */
    public Collection<String> getShematicNames() {
        final File parent = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), Settings.Paths.SCHEMATICS);
        final List<String> names = new ArrayList<>();
        if (parent.exists()) {
            final String[] rawNames = parent.list((dir, name) -> name.endsWith(".schematic"));
            if (rawNames != null) {
                final List<String> transformed = Arrays.stream(rawNames).map(rawName -> rawName.substring(0, rawName.length() - 10))
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
    public Schematic getSchematic(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            return getSchematic(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Schematic getSchematic(URL url) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            InputStream is = Channels.newInputStream(rbc);
            return getSchematic(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Schematic getSchematic(InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            NBTInputStream stream = new NBTInputStream(new GZIPInputStream(is));
            CompoundTag tag = (CompoundTag) stream.readTag(1073741824);
            is.close();
            stream.close();
            return getSchematic(tag);
        } catch (IOException e) {
            e.printStackTrace();
            PlotSquared.debug(is.toString() + " | " + is.getClass().getCanonicalName()
                + " is not in GZIP format : " + e.getMessage());
        }
        return null;
    }

    public List<String> getSaves(UUID uuid) {
        StringBuilder rawJSON = new StringBuilder();
        try {
            String website = Settings.Web.URL + "list.php?" + uuid.toString();
            URL url = new URL(website);
            URLConnection connection = new URL(url.toString()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                rawJSON.append(line);
            }
            reader.close();
            JSONArray array = new JSONArray(rawJSON.toString());
            List<String> schematics = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String schematic = array.getString(i);
                schematics.add(schematic);
            }
            return schematics;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            PlotSquared.debug("ERROR PARSING: " + rawJSON);
        }
        return null;
    }

    public void upload(final CompoundTag tag, UUID uuid, String file, RunnableVal<URL> whenDone) {
        if (tag == null) {
            PlotSquared.debug("&cCannot save empty tag");
            TaskManager.runTask(whenDone);
            return;
        }
        MainUtil.upload(uuid, file, "schematic", new RunnableVal<OutputStream>() {
            @Override public void run(OutputStream output) {
                try {
                    try (GZIPOutputStream gzip = new GZIPOutputStream(output, true)) {
                        try (NBTOutputStream nos = new NBTOutputStream(gzip)) {
                            nos.writeTag(tag);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            PlotSquared.debug("&cCannot save empty tag");
            return false;
        }
        try {
            File tmp = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), path);
            tmp.getParentFile().mkdirs();
            try (OutputStream stream = new FileOutputStream(tmp);
                NBTOutputStream output = new NBTOutputStream(new GZIPOutputStream(stream))) {
                output.writeTag(tag);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Create a compound tag from blocks
     * - Untested
     *
     * @param blocks
     * @param blockData
     * @param dimension
     * @return
     */
    public CompoundTag createTag(byte[] blocks, byte[] blockData, Dimension dimension) {
        HashMap<String, Tag> schematic = new HashMap<>();
        schematic.put("Width", new ShortTag("Width", (short) dimension.getX()));
        schematic.put("Length", new ShortTag("Length", (short) dimension.getZ()));
        schematic.put("Height", new ShortTag("Height", (short) dimension.getY()));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", 0));
        schematic.put("WEOriginY", new IntTag("WEOriginY", 0));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", 0));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", 0));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", 0));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", 0));
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities",
            new ListTag("TileEntities", CompoundTag.class, new ArrayList<Tag>()));
        return new CompoundTag("Schematic", schematic);
    }

    public abstract void getCompoundTag(String world, Set<RegionWrapper> regions,
        RunnableVal<CompoundTag> whenDone);

    public void getCompoundTag(final Plot plot, final RunnableVal<CompoundTag> whenDone) {
        getCompoundTag(plot.getWorldName(), plot.getRegions(), new RunnableVal<CompoundTag>() {
            @Override public void run(CompoundTag value) {
                if (!plot.getFlags().isEmpty()) {
                    HashMap<String, Tag> flagMap = new HashMap<>();
                    for (Map.Entry<Flag<?>, Object> entry : plot.getFlags().entrySet()) {
                        String key = entry.getKey().getName();
                        flagMap.put(key,
                            new StringTag(key, entry.getKey().valueToString(entry.getValue())));
                    }
                    CompoundTag tag = new CompoundTag("Flags", flagMap);
                    HashMap<String, Tag> map = new HashMap<>(value.getValue());
                    map.put("Flags", tag);
                    value.setValue(map);
                }
                whenDone.run(value);
            }
        });
    }

    /**
     * Schematic Dimensions.
     */
    public static class Dimension {

        private final int x;
        private final int y;
        private final int z;

        public Dimension(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }
    }


    /**
     * Schematic Class
     */
    public class Schematic {
        // Lossy but fast
        private final short[] ids;
        private final byte[] datas;
        private final Dimension schematicDimension;
        private Map<String, Tag> flags;
        private HashMap<BlockLoc, CompoundTag> tiles;

        public Schematic(short[] i, byte[] b, Dimension d, Map<String, Tag> flags) {
            this.ids = i;
            this.datas = b;
            this.schematicDimension = d;
            setFlags(flags);
        }

        public Map<String, Tag> getFlags() {
            return this.flags;
        }

        public void setFlags(Map<String, Tag> flags) {
            this.flags = flags == null ? new HashMap<String, Tag>() : flags;
        }

        /**
         * Add a tile entity
         *
         * @param loc
         * @param tag
         */
        public void addTile(BlockLoc loc, CompoundTag tag) {
            if (this.tiles == null) {
                this.tiles = new HashMap<>();
            }
            this.tiles.put(loc, tag);
        }

        /**
         * Get the tile entities
         *
         * @return Map of block location to tag
         */
        public HashMap<BlockLoc, CompoundTag> getTiles() {
            return this.tiles == null ? new HashMap<BlockLoc, CompoundTag>() : this.tiles;
        }

        /**
         * Get the schematic dimensions.
         *
         * @return
         */
        public Dimension getSchematicDimension() {
            return this.schematicDimension;
        }

        /**
         * Get the block type array.
         *
         * @return
         */
        public short[] getIds() {
            return this.ids;
        }

        /**
         * Get the block data array.
         *
         * @return
         */
        public byte[] getDatas() {
            return this.datas;
        }

        public Schematic copySection(RegionWrapper region) {

            int x1 = region.minX;
            int x2 = region.maxX;

            int z1 = region.minZ;
            int z2 = region.maxZ;

            int y1 = region.minY;
            int y2 = Math.min(region.maxY, 255);

            int width = x2 - x1 + 1;
            int length = z2 - z1 + 1;
            int height = y2 - y1 + 1;

            short[] ids2 = new short[width * length * height];
            byte[] datas2 = new byte[width * length * height];

            int dx = this.schematicDimension.getX();
            int dy = this.schematicDimension.getY();
            int dz = this.schematicDimension.getZ();

            for (int y = y1; y <= y2; y++) {
                int yy = y >= 0 ? y < dy ? y : y - dy : y + dy;
                int i1 = yy * dx * dz;
                int j1 = (y - y1) * width * length;
                for (int z = z1; z <= z2; z++) {
                    int zz = z >= 0 ? z < dz ? z : z - dz : z + dz;
                    int i2 = i1 + zz * dx;
                    int j2 = j1 + (z - z1) * width;
                    for (int x = x1; x <= x2; x++) {
                        int xx = x >= 0 ? x < dx ? x : x - dx : x + dx;
                        int i3 = i2 + xx;
                        int j3 = j2 + (x - x1);
                        ids2[j3] = this.ids[i3];
                        datas2[j3] = this.datas[i3];
                    }
                }
            }
            return new Schematic(ids2, datas2, new Dimension(width, height, length), null);
        }

        public void save(File file) {
            byte[] ids2 = new byte[this.ids.length];
            for (int i = 0; i < this.ids.length; i++) {
                ids2[i] = (byte) this.ids[i];
            }
            CompoundTag tag = createTag(ids2, this.datas, this.schematicDimension);
            SchematicHandler.this.save(tag, file.getAbsolutePath());
        }
    }

}
