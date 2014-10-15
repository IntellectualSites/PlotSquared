package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by Citymonstret on 2014-09-15.
 */
public class SchematicHandler {

	public boolean paste(Location location, Schematic schematic, Plot plot) {
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
	                    
	                    Block block = world.getBlockAt(l1.getBlockX()+x, l1.getBlockY()+y, l1.getBlockZ()+z);
	                    
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

	public Schematic getSchematic(String name) {
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

		Schematic schematic = null;
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

	public class Dimension {
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

	public class DataCollection {
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
}
