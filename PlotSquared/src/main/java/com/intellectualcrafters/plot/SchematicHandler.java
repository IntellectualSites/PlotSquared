package com.intellectualcrafters.plot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.jnbt.*;
import com.intellectualcrafters.plot.SchematicHandler.DataCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Schematic Handler
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings({"all"})
public class SchematicHandler {
    /**
     * Paste a schematic
     * @param location origin
     * @param schematic schematic to paste
     * @param plot plot to paste in
     * @return true if succeeded
     */
	public static boolean paste(Location location, Schematic schematic, Plot plot, int x_offset, int z_offset) {
		if (schematic == null) {
			PlotMain.sendConsoleSenderMessage("Schematic == null :|");
			return false;
		}
		try {
		    Dimension demensions = schematic.getSchematicDimension();
	        
		    int WIDTH = demensions.getX();
	        int LENGTH = demensions.getZ();
	        int HEIGHT = demensions.getY();
		    
		    DataCollection[] blocks = schematic.getBlockCollection();
		    
		    
		    
		    Location l1 = PlotHelper.getPlotBottomLoc(plot.getWorld(), plot.getId());
		    
		    int sy = location.getWorld().getHighestBlockYAt(l1.getBlockX()+1, l1.getBlockZ()+1);
		    
		    l1 = l1.add(1, sy-1, 1);
		    
            World world = location.getWorld();
            
		    for (int x = 0; x < WIDTH; x++) {
	            for (int z = 0; z < LENGTH; z++) {
	                for (int y = 0; y < HEIGHT; y++) {
	                    int index = y * WIDTH * LENGTH + z * WIDTH + x;
	                    
	                    short id = blocks[index].getBlock();
	                    byte data = blocks[index].getData();
	                    
	                    Block block = world.getBlockAt(l1.getBlockX()+x+x_offset, l1.getBlockY()+y, l1.getBlockZ()+z+z_offset);
	                    
	                    PlotBlock plotblock = new PlotBlock(id, data);
	                    
	                    PlotHelper.setBlock(block, plotblock);
	                }
	            }
            }
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

    /**
     * Get a schematic
     * @param name to check
     * @return schematic if found, else null
     */
	public static Schematic getSchematic(String name) {
		{
			File parent =
					new File(JavaPlugin.getPlugin(PlotMain.class).getDataFolder() + File.separator + "schematics");
			if (!parent.exists()) {
				parent.mkdir();
			}
		}
		File file =
				new File(JavaPlugin.getPlugin(PlotMain.class).getDataFolder() + File.separator + "schematics"
						+ File.separator + name + ".schematic");
		if (!file.exists()) {
			PlotMain.sendConsoleSenderMessage(file.toString() + " doesn't exist");
			return null;
		}

		Schematic schematic;
		try {
			InputStream iStream = new FileInputStream(file);
			NBTInputStream stream = new NBTInputStream(new GZIPInputStream(iStream));
			CompoundTag tag = (CompoundTag) stream.readTag();
			stream.close();
			Map<String, Tag> tagMap = tag.getValue();

			byte[] addId = new byte[0];
			if (tagMap.containsKey("AddBlocks")) {
				addId = ByteArrayTag.class.cast(tagMap.get("AddBlocks")).getValue();
			}
			short width = ShortTag.class.cast(tagMap.get("Width")).getValue();
			short length = ShortTag.class.cast(tagMap.get("Length")).getValue();
			short height = ShortTag.class.cast(tagMap.get("Height")).getValue();

			byte[] b = ByteArrayTag.class.cast(tagMap.get("Blocks")).getValue();
			byte[] d = ByteArrayTag.class.cast(tagMap.get("Data")).getValue();
			short[] blocks = new short[b.length];

			Dimension dimension = new Dimension(width, height, length);

			for (int index = 0; index < b.length; index++) {
				if ((index >> 1) >= addId.length) { // No corresponding
					// AddBlocks index
					blocks[index] = (short) (b[index] & 0xFF);
				}
				else {
					if ((index & 1) == 0) {
						blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (b[index] & 0xFF));
					}
					else {
						blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (b[index] & 0xFF));
					}
				}
			}

			DataCollection[] collection = new DataCollection[b.length];

			for (int x = 0; x < b.length; x++) {
				collection[x] = new DataCollection(blocks[x], d[x]);
			}

			schematic = new Schematic(collection, dimension, file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return schematic;
	}

    /**
     * Schematic Class
     * @author Citymonstret
     */
	public static class Schematic {
		private DataCollection[] blockCollection;
		private Dimension schematicDimension;
		private File file;

		public Schematic(DataCollection[] blockCollection, Dimension schematicDimension, File file) {
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
     * @author Citymonstret
     */
	public static class Dimension {
		private int x;
		private int y;
		private int z;

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
	 * Saves a schematic to a file path
	 * @param tag to save
	 * @param path to save in
	 * @return true if succeeded
	 */
	public static boolean save(CompoundTag tag, String path) {
	    
	    if (tag==null) {
	        PlotMain.sendConsoleSenderMessage("&cCannot save empty tag");
	        return false;
	    }
	    
        try {
            OutputStream stream = new FileOutputStream(path);
            NBTOutputStream output = new NBTOutputStream(new GZIPOutputStream(stream));
            output.writeTag(tag);
            output.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	/**
	 * Gets the schematic of a plot
	 * @param world to check
	 * @param id plot
	 * @return tag
	 */
	public static CompoundTag getCompoundTag(World world, PlotId id) {

	    if (!PlotMain.getPlots(world).containsKey(id)) {
	        return null;
	        // Plot is empty
	    }
	    
	    // loading chunks
	    final Location pos1 = PlotHelper.getPlotBottomLoc(world, id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, id);
        int i = 0;
        int j = 0;
        try {
        for (i = (pos1.getBlockX() / 16) * 16; i < (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
            for (j = (pos1.getBlockZ() / 16) * 16; j < (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                boolean result = chunk.load(false);
                if (!result) {
                    
                    // Plot is not even generated
                    
                    return null;
                }
            }
        }
        }
        catch (Exception e) {
            PlotMain.sendConsoleSenderMessage("&7 - Cannot save: corrupt chunk at "+(i/16)+", "+(j/16));
            return null;
        }
        int width = pos2.getBlockX()-pos1.getBlockX()+1;
        int height = 256;
        int length = pos2.getBlockZ()-pos1.getBlockZ()+1;

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
        byte[] blocks = new byte[width * height * length];
        byte[] addBlocks = null;
        byte[] blockData = new byte[width * height * length];
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = 0; y < height; y++) {
                    int index = y * width * length + z * width + x;

                    Block block = world.getBlockAt(new Location(world, pos1.getBlockX() + x, y, pos1.getBlockZ() + z));
                    
                    int id2 = block.getTypeId(); 
                    
                    if (id2 > 255) {
                        if (addBlocks == null) {
                            addBlocks = new byte[(blocks.length >> 1) + 1];
                        }

                        addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                addBlocks[index >> 1] & 0xF0 | (id2 >> 8) & 0xF
                                : addBlocks[index >> 1] & 0xF | ((id2 >> 8) & 0xF) << 4);
                    }

                    blocks[index] = (byte) id2;
                    blockData[index] = block.getData();
                    
                    
                    // We need worldedit to save tileentity data or entities
                    //  - it uses NMS and CB internal code, which changes every update
                }
            }
        }
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, new ArrayList<Tag>()));
        
        if (addBlocks != null) {
            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
        }

        return new CompoundTag("Schematic", schematic);
    }

    /**
     * Schematic Data Collection
     * @author Citymonstret
     */
	public static class DataCollection {
		private short block;
		private byte data;

		public DataCollection(short block, byte data) {
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

    public static boolean pastePart(World world, DataCollection[] blocks, Location l1, int x_offset, int z_offset, int i1, int i2, int WIDTH, int LENGTH) {
        boolean result = false;
        for (int i = i1; i<=i2 ;i++) {
            short id = blocks[i].getBlock();
            byte data = blocks[i].getData();
                if (id==0) {
                    continue;
                }
            
            int area = WIDTH*LENGTH;
            int r = i%(area);

            int x = r%WIDTH;
            int y = i/area;
            int z = r/WIDTH;
            
            if (y>256) {
                break;
            }
            
            Block block = world.getBlockAt(l1.getBlockX()+x+x_offset, l1.getBlockY()+y, l1.getBlockZ()+z+z_offset);
            
            PlotBlock plotblock = new PlotBlock(id, data);
            
            boolean set = PlotHelper.setBlock(block, plotblock);
            if (!result && set) {
                result = true;
            }
        }
        return result;
    }
}
