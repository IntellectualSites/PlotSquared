package com.intellectualcrafters.plot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;

import com.intellectualcrafters.jnbt.ByteArrayTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.IntTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.NBTInputStream;
import com.intellectualcrafters.jnbt.NBTOutputStream;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.commands.SchematicCmd;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.object.schematic.StateWrapper;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public abstract class SchematicHandler {
    public static SchematicHandler manager = new BukkitSchematicHandler();
    
    private boolean exportAll = false;
    
    public boolean exportAll(final Collection<Plot> plots, final File outputDir, final String namingScheme, final Runnable ifSuccess) {
    	if (exportAll) {
    		return false;
    	}
    	if (plots.size() == 0) {
    		return false;
    	}
    	exportAll = true;
    	TaskManager.index.increment();
    	final Integer currentIndex = TaskManager.index.toInteger();
        final int task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
            	if (plots.size() == 0) {
            		Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    TaskManager.runTask(ifSuccess);
                    return;
            	}
            	Iterator<Plot> i = plots.iterator();
            	final Plot plot = i.next();
            	i.remove();
                final CompoundTag sch = SchematicHandler.manager.getCompoundTag(plot.world, plot.id);
                String o = UUIDHandler.getName(plot.owner);
                if (o == null) {
                	o = "unknown";
                }
                final String name;
                if (namingScheme == null) {
                	name = plot.id.x + ";" + plot.id.y + "," + plot.world + "," + o;
                }
                else {
                	name = namingScheme.replaceAll("%owner%", o).replaceAll("%id%", plot.id.toString()).replaceAll("%idx%", plot.id.x + "").replaceAll("%idy%", plot.id.y + "").replaceAll("%world%", plot.world);
                }
                final String directory;
                if (outputDir == null) {
                	directory = Settings.SCHEMATIC_SAVE_PATH;
                }
                else {
                	directory = outputDir.getPath();
                }
                if (sch == null) {
                    MainUtil.sendMessage(null, "&7 - Skipped plot &c" + plot.id);
                } else {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(null, "&6ID: " + plot.id);
                            final boolean result = SchematicHandler.manager.save(sch, directory + File.separator + name + ".schematic");
                            if (!result) {
                                MainUtil.sendMessage(null, "&7 - Failed to save &c" + plot.id);
                            } else {
                                MainUtil.sendMessage(null, "&7 - &a  success: " + plot.id);
                            }
                        }
                    });
                }
            }
        }, 20);
        TaskManager.tasks.put(currentIndex, task);
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
    public boolean paste(final Schematic schematic, final Plot plot, final int x_offset, final int z_offset) {
        if (schematic == null) {
            PlotSquared.log("Schematic == null :|");
            return false;
        }
        try {
            final Dimension demensions = schematic.getSchematicDimension();
            final int WIDTH = demensions.getX();
            final int LENGTH = demensions.getZ();
            final int HEIGHT = demensions.getY();
            final DataCollection[] blocks = schematic.getBlockCollection();
            Location l1 = MainUtil.getPlotBottomLoc(plot.world, plot.getId());
            final int sy = BukkitUtil.getHeighestBlock(plot.world, l1.getX() + 1, l1.getZ() + 1);
            if (!(HEIGHT == BukkitUtil.getMaxHeight(plot.world))) {
                l1 = l1.add(1, sy - 1, 1);
            }
            else {
                l1 = l1.add(1, 0, 1);
            }
            int X = l1.getX();
            int Y = l1.getY();
            int Z = l1.getZ();
            final int[] xl = new int[blocks.length];
            final int[] yl = new int[blocks.length];
            final int[] zl = new int[blocks.length];
            final int[] ids = new int[blocks.length];
            final byte[] data = new byte[blocks.length];
            for (int x = 0; x < WIDTH; x++) {
                for (int z = 0; z < LENGTH; z++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        final int index = (y * WIDTH * LENGTH) + (z * WIDTH) + x;
                        final DataCollection block = blocks[index];
                        xl[index] = x + X;
                        yl[index] = y + Y;
                        zl[index] = z + Z;
                        ids[index] = block.block;
                        data[index] = block.data;
                    }
                }
            }
            BlockManager.setBlocks(plot.world, xl, yl, zl, ids, data);
            pasteStates(schematic, plot, x_offset, z_offset);
        } catch (final Exception e) {
            return false;
        }
        return true;
    }
    
    public boolean pasteStates(final Schematic schematic, final Plot plot, final int x_offset, final int z_offset) {
        if (schematic == null) {
            PlotSquared.log("Schematic == null :|");
            return false;
        }
        HashSet<PlotItem> items = schematic.getItems();
        if (items == null) {
            return false;
        }
        Location l1 = MainUtil.getPlotBottomLoc(plot.world, plot.getId());
        final int sy = BukkitUtil.getHeighestBlock(plot.world, l1.getX() + 1, l1.getZ() + 1);
        final Dimension demensions = schematic.getSchematicDimension();
        final int HEIGHT = demensions.getY();
        if (!(HEIGHT == BukkitUtil.getMaxHeight(plot.world))) {
            l1 = l1.add(1, sy - 1, 1);
        } else {
            l1 = l1.add(1, 0, 1);
        }
        int X = l1.getX() + x_offset;
        int Y = l1.getY();
        int Z = l1.getZ() + z_offset;
        for (PlotItem item : items) {
            item.x += X;
            item.y += Y;
            item.z += Z;
            BlockManager.manager.addItems(plot.world, item);
        }
        return true;
    }

    public Schematic getSchematic(final CompoundTag tag, final File file) {
        final Map<String, Tag> tagMap = tag.getValue();
        byte[] addId = new byte[0];
        if (tagMap.containsKey("AddBlocks")) {
            addId = ByteArrayTag.class.cast(tagMap.get("AddBlocks")).getValue();
        }
        final short width = ShortTag.class.cast(tagMap.get("Width")).getValue();
        final short length = ShortTag.class.cast(tagMap.get("Length")).getValue();
        final short height = ShortTag.class.cast(tagMap.get("Height")).getValue();
        final byte[] b = ByteArrayTag.class.cast(tagMap.get("Blocks")).getValue();
        final byte[] d = ByteArrayTag.class.cast(tagMap.get("Data")).getValue();
        final short[] blocks = new short[b.length];
        final Dimension dimension = new Dimension(width, height, length);
        for (int index = 0; index < b.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding
                // AddBlocks index
                blocks[index] = (short) (b[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (b[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (b[index] & 0xFF));
                }
            }
        }
        final DataCollection[] collection = new DataCollection[b.length];
        for (int x = 0; x < b.length; x++) {
            collection[x] = new DataCollection(blocks[x], d[x]);
        }
        Schematic schem = new Schematic(collection, dimension, file);
        try {
            List<Tag> blockStates = ListTag.class.cast(tagMap.get("TileEntities")).getValue();
            for (Tag stateTag : blockStates) {
                CompoundTag ct = ((CompoundTag) stateTag);
                Map<String, Tag> state = ct.getValue();
                short x = IntTag.class.cast(state.get("x")).getValue().shortValue();
                short y = IntTag.class.cast(state.get("y")).getValue().shortValue();
                short z = IntTag.class.cast(state.get("z")).getValue().shortValue();
                new StateWrapper(ct).restoreTag(x, y, z, schem);
            }
        }
        catch (Exception e) {
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
            final File parent = new File(PlotSquared.IMP.getDirectory() + File.separator + "schematics");
            if (!parent.exists()) {
                if (!parent.mkdir()) {
                    throw new RuntimeException("Could not create schematic parent directory");
                }
            }
        }
        final File file = new File(PlotSquared.IMP.getDirectory() + File.separator + "schematics" + File.separator + name + ".schematic");
        return getSchematic(file);
    }
    
    /**
     * Get a schematic
     *
     * @param name to check
     *
     * @return schematic if found, else null
     */
    public Schematic getSchematic(File file) {
        if (!file.exists()) {
            PlotSquared.log(file.toString() + " doesn't exist");
            return null;
        }
        try {
            final InputStream iStream = new FileInputStream(file);
            final NBTInputStream stream = new NBTInputStream(new GZIPInputStream(iStream));
            final CompoundTag tag = (CompoundTag) stream.readTag();
            stream.close();
            return getSchematic(tag, file);
        } catch (final Exception e) {
            PlotSquared.log(file.toString() + " is not in GZIP format");
            return null;
        }
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
            PlotSquared.log("&cCannot save empty tag");
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
     * Gets the schematic of a plot
     *
     * @param world to check
     * @param id    plot
     *
     * @return tag
     */
    public CompoundTag getCompoundTag(final String world, final PlotId id) {
        if (!PlotSquared.getPlots(world).containsKey(id)) {
            return null;
        }
        final Location pos1 = MainUtil.getPlotBottomLoc(world, id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(world, id);
        return getCompoundTag(world, pos1, pos2);
    }
    
    public abstract CompoundTag getCompoundTag(final String world, final Location pos1, final Location pos2);
    
    public boolean pastePart(final String world, final DataCollection[] blocks, final Location l1, final int x_offset, final int z_offset, final int i1, final int i2, final int WIDTH, final int LENGTH) {
        int length = 0;
        for (int i = i1; i <= i2; i++) {
            if (blocks[i].block == 0) {
                length++;
            }
        }
        length = i2 - i1 - length + 1;

        int X = l1.getX();
        int Y = l1.getY();
        int Z = l1.getZ();
        
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
     * @author Citymonstret
     */
    public class Schematic {
        private final DataCollection[] blockCollection;
        private final Dimension schematicDimension;
        private final File file;
        private HashSet<PlotItem> items;

        public  void addItem(PlotItem item) {
            if (this.items == null) {
                this.items = new HashSet<>();
            }
            items.add(item);
        }
        
        public HashSet<PlotItem> getItems() {
            return this.items;
        }
        
        public Schematic(final DataCollection[] blockCollection, final Dimension schematicDimension, final File file) {
            this.blockCollection = blockCollection;
            this.schematicDimension = schematicDimension;
            this.file = file;
        }

        public File getFile() {
            return this.file;
        }

        public Dimension getSchematicDimension() {
            return this.schematicDimension;
        }

        public DataCollection[] getBlockCollection() {
            return this.blockCollection;
        }
    }

    /**
     * Schematic Dimensions
     *
     * @author Citymonstret
     */
    public class Dimension {
        private final int x;
        private final int y;
        private final int z;

        public Dimension(final int x, final int y, final int z) {
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
     * Schematic Data Collection
     *
     * @author Citymonstret
     */
    public class DataCollection {
        private final short block;
        private final byte data;

        // public CompoundTag tag;
        public DataCollection(final short block, final byte data) {
            this.block = block;
            this.data = data;
        }

        public short getBlock() {
            return this.block;
        }

        public byte getData() {
            return this.data;
        }
    }
}
