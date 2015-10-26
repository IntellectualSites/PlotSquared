package com.intellectualcrafters.plot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.collect.Lists;
import com.intellectualcrafters.jnbt.ByteArrayTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.IntTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.NBTInputStream;
import com.intellectualcrafters.jnbt.NBTOutputStream;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.StringTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.json.JSONArray;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.plotsquared.object.schematic.StateWrapper;

public abstract class SchematicHandler {
    public static SchematicHandler manager;
    
    private boolean exportAll = false;
    
    public boolean exportAll(final Collection<Plot> collection, final File outputDir, final String namingScheme, final Runnable ifSuccess) {
        if (exportAll) {
            return false;
        }
        if (collection.size() == 0) {
            return false;
        }
        exportAll = true;
        final ArrayList<Plot> plots = new ArrayList<Plot>(collection);
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                if (plots.size() == 0) {
                    exportAll = false;
                    TaskManager.runTask(ifSuccess);
                    return;
                }
                final Iterator<Plot> i = plots.iterator();
                final Plot plot = i.next();
                i.remove();
                String o = UUIDHandler.getName(plot.owner);
                if (o == null) {
                    o = "unknown";
                }
                final String name;
                if (namingScheme == null) {
                    name = plot.id.x + ";" + plot.id.y + "," + plot.world + "," + o;
                } else {
                    name = namingScheme.replaceAll("%owner%", o).replaceAll("%id%", plot.id.toString()).replaceAll("%idx%", plot.id.x + "").replaceAll("%idy%", plot.id.y + "")
                    .replaceAll("%world%", plot.world);
                }
                final String directory;
                if (outputDir == null) {
                    directory = Settings.SCHEMATIC_SAVE_PATH;
                } else {
                    directory = outputDir.getPath();
                }
                final Runnable THIS = this;
                SchematicHandler.manager.getCompoundTag(plot.world, plot.id, new RunnableVal<CompoundTag>() {
                    @Override
                    public void run() {
                        if (value == null) {
                            MainUtil.sendMessage(null, "&7 - Skipped plot &c" + plot.id);
                        } else {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.sendMessage(null, "&6ID: " + plot.id);
                                    final boolean result = SchematicHandler.manager.save(value, directory + File.separator + name + ".schematic");
                                    if (!result) {
                                        MainUtil.sendMessage(null, "&7 - Failed to save &c" + plot.id);
                                    } else {
                                        MainUtil.sendMessage(null, "&7 - &a  success: " + plot.id);
                                    }
                                    TaskManager.runTask(new Runnable() {
                                        @Override
                                        public void run() {
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
     * Paste a schematic
     *
     * @param schematic the schematic object to paste
     * @param plot      plot to paste in
     * @param x_offset  offset x to paste it from plot origin
     * @param z_offset  offset z to paste it from plot origin
     *
     * @return boolean true if succeeded
     */
    public void paste(final Schematic schematic, final Plot plot, final int x_offset, final int z_offset, final RunnableVal<Boolean> whenDone) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                if (whenDone != null) {
                    whenDone.value = false;
                }
                if (schematic == null) {
                    PS.debug("Schematic == null :|");
                    TaskManager.runTask(whenDone);
                    return;
                }
                try {
                    final Dimension demensions = schematic.getSchematicDimension();
                    final int WIDTH = demensions.getX();
                    final int LENGTH = demensions.getZ();
                    final int HEIGHT = demensions.getY();
                    // Validate dimensions
                    RegionWrapper region = MainUtil.getLargestRegion(plot);
                    if ((((region.maxX - region.minX + x_offset) + 1) < WIDTH) || (((region.maxZ - region.minZ + z_offset) + 1) < LENGTH) || (HEIGHT > 256)) {
                        PS.debug("Schematic is too large");
                        PS.debug("(" + WIDTH + "," + LENGTH + "," + HEIGHT + ") is bigger than (" + (region.maxX - region.minX) + "," + (region.maxZ - region.minZ) + ",256)");
                        TaskManager.runTask(whenDone);
                        return;
                    }
                    // block id and data arrays
                    final short[] ids = schematic.ids;
                    final byte[] datas = schematic.datas;
                    // Calculate the optimal height to paste the schematic at
                    final int y_offset;
                    if (HEIGHT >= 256) {
                        y_offset = 0;
                    } else {
                        y_offset = MainUtil.getHeighestBlock(plot.world, region.minX + 1, region.minZ + 1);
                    }
                    final Location pos1 = new Location(plot.world, region.minX + x_offset, y_offset, region.minZ + z_offset);
//                    Location pos2 = new Location(plot.world, region.maxX, region.maxY, region.maxZ);
                    final Location pos2 = pos1.clone().add(WIDTH - 1, HEIGHT - 1, LENGTH - 1);
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
                        @Override
                        public void run() {
                            int count = 0;
                            while ((chunks.size() > 0) && (count < 256)) {
                                count++;
                                final ChunkLoc chunk = chunks.remove(0);
                                final int x = chunk.x;
                                final int z = chunk.z;
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
                                int id;
                                
                                for (int ry = 0; ry < Math.min(256, HEIGHT); ry++) {
                                    final int yy = y_offset + ry;
                                    if (yy > 255) {
                                        continue;
                                    }
                                    final int i1 = ry * WIDTH * LENGTH;
                                    for (int rz = zzb - p1z; rz <= (zzt - p1z); rz++) {
                                        final int i2 = (rz * WIDTH) + i1;
                                        for (int rx = xxb - p1x; rx <= (xxt - p1x); rx++) {
                                            final int i = i2 + rx;
                                            
                                            final int xx = p1x + rx;
                                            final int zz = p1z + rz;
                                            
                                            id = ids[i];
                                            
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
                                                    SetBlockQueue.setBlock(plot.world, xx, yy, zz, id);
                                                    break;
                                                }
                                                default: {
                                                    SetBlockQueue.setBlock(plot.world, xx, yy, zz, new PlotBlock((short) id, datas[i]));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (chunks.size() != 0) {
                                final Runnable task = this;
                                // Run when the queue is free
                                SetBlockQueue.addNotify(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.gc();
                                        TaskManager.runTaskLaterAsync(task, 80);
                                    }
                                });
                            } else {
                                System.gc();
                                // Finished
                                SetBlockQueue.addNotify(new Runnable() {
                                    @Override
                                    public void run() {
                                        pasteStates(schematic, plot, x_offset, z_offset);
                                        if (whenDone != null) {
                                            whenDone.value = true;
                                            whenDone.run();
                                        }
                                    }
                                });
                                return;
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    TaskManager.runTask(whenDone);
                    return;
                }
            }
        });
    }
    
    public boolean pasteStates(final Schematic schematic, final Plot plot, final int x_offset, final int z_offset) {
        if (schematic == null) {
            PS.debug("Schematic == null :|");
            return false;
        }
        final HashSet<PlotItem> items = schematic.getItems();
        if (items == null) {
            return false;
        }
        RegionWrapper region = MainUtil.getLargestRegion(plot);
        Location l1 = new Location(plot.world, region.minX + x_offset, 1, region.minZ + z_offset);
//        Location l1 = MainUtil.getPlotBottomLoc(plot.world, plot.getId());
        final int sy = MainUtil.getHeighestBlock(plot.world, l1.getX() + 1, l1.getZ() + 1);
        final Dimension demensions = schematic.getSchematicDimension();
        final int HEIGHT = demensions.getY();
        if ((HEIGHT < 255)) {
            l1 = l1.add(0, sy - 1, 0);
        }
        final int X = l1.getX() + x_offset;
        final int Y = l1.getY();
        final int Z = l1.getZ() + z_offset;
        for (final PlotItem item : items) {
            item.x += X;
            item.y += Y;
            item.z += Z;
            BlockManager.manager.addItems(plot.world, item);
        }
        return true;
    }
    
    public Schematic getSchematic(final CompoundTag tag) {
        final Map<String, Tag> tagMap = tag.getValue();
        // Slow
        //        byte[] addId = new byte[0];
        //        if (tagMap.containsKey("AddBlocks")) {
        //            addId = ByteArrayTag.class.cast(tagMap.get("AddBlocks")).getValue();
        //        }
        // end slow
        
        final short width = ShortTag.class.cast(tagMap.get("Width")).getValue();
        final short length = ShortTag.class.cast(tagMap.get("Length")).getValue();
        final short height = ShortTag.class.cast(tagMap.get("Height")).getValue();
        final byte[] block_sml = ByteArrayTag.class.cast(tagMap.get("Blocks")).getValue();
        final byte[] data = ByteArrayTag.class.cast(tagMap.get("Data")).getValue();
        
        final short[] block = new short[block_sml.length];
        for (int i = 0; i < block.length; i++) {
            short id = block_sml[i];
            if (id < 0) {
                id = (short) (id & 0xFF);
            }
            block[i] = id;
        }
        
        // Slow + has code for exceptions (addId) inside the loop rather than outside
        //        for (int index = 0; index < b.length; index++) {
        //            if ((index >> 1) >= addId.length) {
        //                blocks[index] = (short) (b[index] & 0xFF);
        //            } else {
        //                if ((index & 1) == 0) {
        //                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (b[index] & 0xFF));
        //                } else {
        //                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (b[index] & 0xFF));
        //                }
        //            }
        //        }
        // Slow as wrapper for each block
        //        final DataCollection[] collection = new DataCollection[b.length];
        //        for (int x = 0; x < b.length; x++) {
        //            collection[x] = new DataCollection(blocks[x], d[x]);
        //        }
        //        Schematic schem = new Schematic(collection, dimension, file);
        
        final Dimension dimensions = new Dimension(width, height, length);
        final Schematic schem = new Schematic(block, data, dimensions);
        
        // Slow
        try {
            final List<Tag> blockStates = ListTag.class.cast(tagMap.get("TileEntities")).getValue();
            for (final Tag stateTag : blockStates) {
                try {
                    final CompoundTag ct = ((CompoundTag) stateTag);
                    final Map<String, Tag> state = ct.getValue();
                    final short x = IntTag.class.cast(state.get("x")).getValue().shortValue();
                    final short y = IntTag.class.cast(state.get("y")).getValue().shortValue();
                    final short z = IntTag.class.cast(state.get("z")).getValue().shortValue();
                    new StateWrapper(ct).restoreTag(x, y, z, schem);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return schem;
    }
    
    /**
     * Get a schematic
     *
     * @param name to check
     *
     * @return schematic if found, else null
     */
    public Schematic getSchematic(final String name) {
        {
            final File parent = new File(PS.get().IMP.getDirectory() + File.separator + "schematics");
            if (!parent.exists()) {
                if (!parent.mkdir()) {
                    throw new RuntimeException("Could not create schematic parent directory");
                }
            }
        }
        final File file = new File(PS.get().IMP.getDirectory() + File.separator + "schematics" + File.separator + name + (name.endsWith(".schematic") ? "" : ".schematic"));
        return getSchematic(file);
    }
    
    /**
     * Get a schematic
     *
     * @param name to check
     *
     * @return schematic if found, else null
     */
    public Schematic getSchematic(final File file) {
        if (!file.exists()) {
            PS.debug(file.toString() + " doesn't exist");
            return null;
        }
        try {
            return getSchematic(new FileInputStream(file));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Schematic getSchematic(final URL url) {
        try {
            final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            final InputStream is = Channels.newInputStream(rbc);
            return getSchematic(is);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Schematic getSchematic(final InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            final NBTInputStream stream = new NBTInputStream(new GZIPInputStream(is));
            final CompoundTag tag = (CompoundTag) stream.readTag(1073741824);
            is.close();
            stream.close();
            return getSchematic(tag);
        } catch (final Exception e) {
            e.printStackTrace();
            PS.debug(is.toString() + " | " + is.getClass().getCanonicalName() + " is not in GZIP format : " + e.getMessage());
        }
        return null;
    }
    
    public List<String> getSaves(final UUID uuid) {
        try {
            final String website = Settings.WEB_URL + "list.php?" + uuid.toString();
            final URL url = new URL(website);
            final URLConnection connection = new URL(url.toString()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            final StringBuilder rawJSON = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                rawJSON.append(line);
            }
            reader.close();
            final JSONArray array = new JSONArray(rawJSON.toString());
            final List<String> schematics = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                final String schematic = array.getString(i);
                schematics.add(schematic);
            }
            return Lists.reverse(schematics);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public URL upload(final CompoundTag tag, UUID uuid, String file) {
        if (tag == null) {
            PS.debug("&cCannot save empty tag");
            return null;
        }
        try {
            String website;
            if (uuid == null) {
                uuid = UUID.randomUUID();
                website = Settings.WEB_URL + "upload.php?" + uuid;
                file = "plot";
            } else {
                website = Settings.WEB_URL + "save.php?" + uuid;
            }
            final String charset = "UTF-8";
            final String param = "value";
            final String boundary = Long.toHexString(System.currentTimeMillis());
            final String CRLF = "\r\n";
            final URLConnection con = new URL(website).openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream output = con.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true)) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).append(param).append(CRLF).flush();
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"schematicFile\"; filename=\"" + file + ".schematic" + "\"").append(CRLF);
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file + ".schematic")).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                final GZIPOutputStream gzip = new GZIPOutputStream(output);
                final NBTOutputStream nos = new NBTOutputStream(gzip);
                nos.writeTag(tag);
                gzip.finish();
                nos.flush();
                output.flush();
                writer.append(CRLF).flush();
                writer.append("--" + boundary + "--").append(CRLF).flush();
                nos.close();
                output.close();
            }
            try (Reader response = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                final char[] buffer = new char[256];
                final StringBuilder result = new StringBuilder();
                while (true) {
                    final int r = response.read(buffer);
                    if (r < 0) {
                        break;
                    }
                    result.append(buffer, 0, r);
                }
                if (!result.toString().equals("The file plot.schematic has been uploaded.")) {
                    PS.debug(result);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            final int responseCode = ((HttpURLConnection) con).getResponseCode();
            if (responseCode != 200) {
                return null;
            }
            return new URL(Settings.WEB_URL + "?key=" + uuid + "&ip=" + Settings.WEB_IP);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Saves a schematic to a file path
     *
     * @param tag  to save
     * @param path to save in
     *
     * @return true if succeeded
     */
    public boolean save(final CompoundTag tag, final String path) {
        if (tag == null) {
            PS.debug("&cCannot save empty tag");
            return false;
        }
        try {
            final File tmp = new File(path);
            tmp.getParentFile().mkdirs();
            final OutputStream stream = new FileOutputStream(path);
            final NBTOutputStream output = new NBTOutputStream(new GZIPOutputStream(stream));
            output.writeTag(tag);
            output.close();
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Create a compound tag from blocks
     *  - Untested
     * @param blocks
     * @param blockdata
     * @param d
     * @return
     */
    public CompoundTag createTag(final byte[] blocks, final byte[] blockdata, final Dimension d) {
        final HashMap<String, Tag> schematic = new HashMap<>();
        schematic.put("Width", new ShortTag("Width", (short) d.getX()));
        schematic.put("Length", new ShortTag("Length", (short) d.getZ()));
        schematic.put("Height", new ShortTag("Height", (short) d.getY()));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", 0));
        schematic.put("WEOriginY", new IntTag("WEOriginY", 0));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", 0));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", 0));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", 0));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", 0));
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockdata));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, new ArrayList<Tag>()));
        return new CompoundTag("Schematic", schematic);
    }
    
    public abstract void getCompoundTag(final String world, Set<RegionWrapper> regions, final RunnableVal<CompoundTag> whenDone);
    
    public void getCompoundTag(final String world, PlotId id, final RunnableVal<CompoundTag> whenDone) {
        getCompoundTag(world, MainUtil.getRegions(MainUtil.getPlotAbs(world, id)), whenDone);
    }
    
    public boolean pastePart(final String world, final DataCollection[] blocks, final Location l1, final int x_offset, final int z_offset, final int i1, final int i2, final int WIDTH, final int LENGTH) {
        int length = 0;
        for (int i = i1; i <= i2; i++) {
            if (blocks[i].block == 0) {
                length++;
            }
        }
        length = (i2 - i1 - length) + 1;
        
        final int X = l1.getX();
        final int Y = l1.getY();
        final int Z = l1.getZ();
        
        final int[] xl = new int[length];
        final int[] yl = new int[length];
        final int[] zl = new int[length];
        final int[] ids = new int[length];
        final byte[] data = new byte[length];
        int count = 0;
        for (int i = i1; i <= i2; i++) {
            final short id = blocks[i].block;
            if (id == 0) {
                continue; //
            }
            final int area = WIDTH * LENGTH;
            final int r = i % (area);
            final int x = r % WIDTH;
            final int y = i / area;
            final int z = r / WIDTH;
            xl[count] = x + X;
            yl[count] = y + Y;
            zl[count] = z + Z;
            ids[count] = id;
            data[count] = blocks[i].data;
            count++;
            if (y > 256) {
                break;
            }
        }
        BlockManager.setBlocks(world, xl, yl, zl, ids, data);
        return true;
    }
    
    /**
     * Schematic Class
     *
     */
    public class Schematic {
        // Lossy but fast
        private final short[] ids;
        private final byte[] datas;
        
        @Deprecated
        private DataCollection[] collection;
        
        private final Dimension schematicDimension;
        private HashSet<PlotItem> items;
        
        /**
         * This is deprecated as having a wrapper for each block is slow.<br>
         *  - There's also a performance hit by having to cast the DataCollection short / byte
         *  -
         * @param blockCollection
         * @param schematicDimension
         * @param file
         */
        @Deprecated
        public Schematic(final DataCollection[] blockCollection, final Dimension schematicDimension) {
            ids = new short[blockCollection.length];
            datas = new byte[blockCollection.length];
            for (int i = 0; i < blockCollection.length; i++) {
                final DataCollection block = blockCollection[i];
                ids[i] = (byte) block.block;
                datas[i] = block.data;
            }
            collection = blockCollection;
            this.schematicDimension = schematicDimension;
        }
        
        public Schematic(final short[] i, final byte[] b, final Dimension d) {
            ids = i;
            datas = b;
            schematicDimension = d;
        }
        
        /**
         * Add an item to the schematic
         * @param item
         */
        public void addItem(final PlotItem item) {
            if (items == null) {
                items = new HashSet<>();
            }
            items.add(item);
        }
        
        /**
         * Get any items associated with this schematic
         * @return
         */
        public HashSet<PlotItem> getItems() {
            return items;
        }
        
        /**
         * Get the schematic dimensions
         * @return
         */
        public Dimension getSchematicDimension() {
            return schematicDimension;
        }
        
        /**
         * Get the block id array
         * @return
         */
        public short[] getIds() {
            return ids;
        }
        
        /**
         * Get the block data array
         * @return
         */
        public byte[] getDatas() {
            return datas;
        }
        
        /**
         * @deprecated as it is slow to wrap each block
         * @return DataCollection of schematic blocks
         */
        @Deprecated
        public DataCollection[] getBlockCollection() {
            if (collection == null) {
                collection = new DataCollection[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    collection[i] = new DataCollection(ids[i], datas[i]);
                }
            }
            return collection;
        }
    }
    
    /**
     * Schematic Dimensions
     *
     */
    public static class Dimension {
        private final int x;
        private final int y;
        private final int z;
        
        public Dimension(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getZ() {
            return z;
        }
    }
    
    /**
     * Schematic Data Collection
     * @deprecated as it is slow to wrap each block
     */
    @Deprecated
    public class DataCollection {
        private final short block;
        private final byte data;
        
        // public CompoundTag tag;
        public DataCollection(final short block, final byte data) {
            this.block = block;
            this.data = data;
        }
        
        public short getBlock() {
            return block;
        }
        
        public byte getData() {
            return data;
        }
    }
}
