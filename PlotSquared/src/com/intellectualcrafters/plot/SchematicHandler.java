package com.intellectualcrafters.plot;

import com.sk89q.jnbt.*;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by Citymonstret on 2014-09-15.
 */
public class SchematicHandler {

    @SuppressWarnings("deprecation")
    public boolean paste(Location location, Schematic schematic) {
        if(schematic == null) {
            PlotMain.sendConsoleSenderMessage("Schematic == null :|");
            return false;
        }

        Dimension dimension = schematic.getSchematicDimension();
        DataCollection[] collection = schematic.getBlockCollection();
        World world = location.getWorld();

        for(int x = 0; x < dimension.getX(); x++) {
            for(int y = 0; y < dimension.getY(); y++) {
                for(int z = 0; z < dimension.getZ(); z++) {
                    DataCollection current = collection[getCurrent(x, y, z, dimension)];
                    (new Location(world, location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z).getBlock()).setTypeIdAndData(current.getBlock(), current.getData(), true);
                }
            }
        }
        return true;
    }

    public Schematic getSchematic(String name) {
        {
            File parent = new File(PlotMain.getPlugin(PlotMain.class).getDataFolder() + File.separator + "schematics");
            if(!parent.exists()) {
                parent.mkdir();
            }
        }
        File file = new File(PlotMain.getPlugin(PlotMain.class).getDataFolder() + File.separator + "schematics" + File.separator + name + ".schematic");
        if(!file.exists()) {
            PlotMain.sendConsoleSenderMessage(file.toString() + " doesn't exist");
            return null;
        }

        Schematic schematic = null;
        try {
            InputStream iStream = new FileInputStream(file);
            NBTInputStream stream = new NBTInputStream(new GZIPInputStream(iStream));
            CompoundTag tag = (CompoundTag) stream.readTag();
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
                if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                    blocks[index] = (short) (b[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (b[index] & 0xFF));
                    } else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (b[index] & 0xFF));
                    }
                }
            }

            DataCollection[] collection = new DataCollection[b.length];

            for(int x = 0; x < b.length; x++) {
                collection[x] = new DataCollection(blocks[x], d[x]);
            }

            schematic = new Schematic(collection, dimension);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            return schematic;
        }
    }

    private int getCurrent(int x, int y, int z, Dimension dimension) {
        return (x * dimension.getX()) + (y * dimension.getY()) + (z * dimension.getZ());
    }

    public class Schematic {
        private DataCollection[] blockCollection;
        private Dimension schematicDimension;

        public Schematic(DataCollection[] blockCollection, Dimension schematicDimension) {
            this.blockCollection = blockCollection;
            this.schematicDimension = schematicDimension;
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
