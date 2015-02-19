////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.generator;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.DataCollection;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;

public class HybridPlotWorld extends ClassicPlotWorld {
    public boolean ROAD_SCHEMATIC_ENABLED;
    public short SCHEMATIC_HEIGHT;
    public boolean PLOT_SCHEMATIC = false;
    public short REQUIRED_CHANGES = 0;
    
    public short PATH_WIDTH_LOWER;
    public short PATH_WIDTH_UPPER;

    /*
     * Here we are just calling the super method, nothing special
     */
    public HybridPlotWorld(final String worldname) {
        super(worldname);
    }
    
    public HashMap<ChunkLoc, HashMap<Short, Short>> G_SCH;
    public HashMap<ChunkLoc, HashMap<Short, Byte>> G_SCH_DATA;
    
    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            PlotSquared.log(" - &cConfiguration is null? (" + config.getCurrentPath() + ")");
        }
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.MAIN_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.filling"), ','));
        this.TOP_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.floor"), ','));
        this.WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block"));
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        this.ROAD_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.block"));
        this.WALL_FILLING = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.filling"));
        this.WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block_claimed"));
        
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
        
        if ((this.ROAD_WIDTH % 2) == 0) {
            PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2) - 1);
        } else {
            PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2));
        }
        
        this.PATH_WIDTH_UPPER = (short) (this.PATH_WIDTH_LOWER + this.PLOT_WIDTH + 1);
        try {
            setupSchematics();
        } catch (Exception e) {
            PlotSquared.log("&c - road schematics are disabled for this world.");
            this.ROAD_SCHEMATIC_ENABLED = false;
        }
        System.out.print("LOADED!");
    }
    
    public void setupSchematics() {
        G_SCH_DATA = new HashMap<>();
        G_SCH = new HashMap<>();
        String schem1Str = "GEN_ROAD_SCHEMATIC/" + worldname + "/sideroad";
        String schem2Str = "GEN_ROAD_SCHEMATIC/" + worldname + "/intersection";
        String schem3Str = "GEN_ROAD_SCHEMATIC/" + worldname + "/plot";
        
        Schematic schem1 = SchematicHandler.getSchematic(schem1Str);
        Schematic schem2 = SchematicHandler.getSchematic(schem2Str);
        Schematic schem3 = SchematicHandler.getSchematic(schem3Str);
        
        int shift = (int) Math.floor(this.ROAD_WIDTH / 2);
        int oddshift = 0;
        if (this.ROAD_WIDTH % 2 != 0) {
            oddshift = 1;
        }
        
        if (schem3 != null) {
            PLOT_SCHEMATIC = true;
            DataCollection[] blocks3 = schem3.getBlockCollection();
            Dimension d3 = schem3.getSchematicDimension();
            short w3 = (short) d3.getX();
            short l3 = (short) d3.getZ();
            short h3 = (short) d3.getY();
            
            int center_shift_x = 0;
            int center_shift_z = 0;
            if (l3  < PLOT_WIDTH) {
                center_shift_z = (int) ((PLOT_WIDTH - l3)/2);
            }
            if (w3  < PLOT_WIDTH) {
                center_shift_x = (int) ((PLOT_WIDTH - w3)/2);
            }
            
            for (short x = 0; x < w3; x++) {
                for (short z = 0; z < l3; z++) {
                    for (short y = 0; y < h3; y++) {
                        int index = y * w3 * l3 + z * w3 + x;
                        short id = blocks3[index].getBlock();
                        byte data = blocks3[index].getData();
                        if (id != 0) {
                            addOverlayBlock((short) (x + shift + oddshift + center_shift_x), (short) (y), (short) (z + shift + oddshift + center_shift_z), id, data, false);
                        }
                    }
                }
            }
        }
        
        if (schem1 == null || schem2 == null || this.ROAD_WIDTH == 0) {
            PlotSquared.log(C.PREFIX.s() + "&3 - schematic: &7false");
            return;
        }
        // Do not populate road if using schematic population
        this.ROAD_BLOCK = new PlotBlock(this.ROAD_BLOCK.id, (byte) 0);
        
        DataCollection[] blocks1 = schem1.getBlockCollection();
        DataCollection[] blocks2 = schem2.getBlockCollection();
        
        Dimension d1 = schem1.getSchematicDimension();
        short w1 = (short) d1.getX();
        short l1 = (short) d1.getZ();
        short h1 = (short) d1.getY();
        
        Dimension d2 = schem2.getSchematicDimension();
        short w2 = (short) d2.getX();
        short l2 = (short) d2.getZ();
        short h2 = (short) d2.getY();
        this.SCHEMATIC_HEIGHT = (short) Math.max(h2, h1);

        for (short x = 0; x < w1; x++) {
            for (short z = 0; z < l1; z++) {
                for (short y = 0; y < h1; y++) {
                    int index = y * w1 * l1 + z * w1 + x;
                    
                    short id = blocks1[index].getBlock();
                    byte data = blocks1[index].getData();
                    
                    if (id != 0) {
                        addOverlayBlock((short) (x - (shift)), (short) (y), (short) (z + shift + oddshift), id, data, false);
                        addOverlayBlock((short) (z + shift + oddshift), (short) (y), (short) (x - shift), id, data, true);
                    }
                }
            }
        }
        
        for (short x = 0; x < w2; x++) {
            for (short z = 0; z < l2; z++) {
                for (short y = 0; y < h2; y++) {
                    int index = y * w2 * l2 + z * w2 + x;
                    short id = blocks2[index].getBlock();
                    byte data = blocks2[index].getData();
                    if (id != 0) {
                        addOverlayBlock((short) (x - shift), (short) (y), (short) (z - shift), id, data, false);
                    }
                }
            }
        }
        this.ROAD_SCHEMATIC_ENABLED = true;
    }
    
    public static boolean isRotate(short id) {
        switch (id) {
            case 23:
                return true;
            case 26:
                return true;
            case 27:
                return true;
            case 28:
                return true;
            case 29:
                return true;
            case 33:
                return true;
            case 53:
                return true;
            case 54:
                return true;
            case 55:
                return true;
            case 61:
                return true;
            case 62:
                return true;
            case 64:
                return true;
            case 65:
                return true;
            case 68:
                return true;
            case 71:
                return true;
            case 77:
                return true;
            case 86:
                return true;
            case 84:
                return true;
            case 93:
                return true;
            case 94:
                return true;
            case 96:
                return true;
            case 107:
                return true;
            case 108:
                return true;
            case 109:
                return true;
            case 111:
                return true;
            case 119:
                return true;
            case 128:
                return true;
            case 130:
                return true;
            case 131:
                return true;
            case 134:
                return true;
            case 135:
                return true;
            case 136:
                return true;
            case 143:
                return true;
            case 144:
                return true;
            case 145:
                return true;
            case 146:
                return true;
            case 149:
                return true;
            case 150:
                return true;
            case 156:
                return true;
            case 157:
                return true;
            case 158:
                return true;
            case 163:
                return true;
            case 164:
                return true;
            case 167:
                return true;
            case 180:
                return true;
            case 183:
                return true;
            case 184:
                return true;
            case 185:
                return true;
            case 186:
                return true;
            case 187:
                return true;
            case 193:
                return true;
            case 194:
                return true;
            case 195:
                return true;
            case 196:
                return true;
            case 197:
                return true;
            default:
                return false;
        }
    }
    
    public void addOverlayBlock(short x, short y, short z, short id, byte data, boolean rotate) {
        if (z < 0) {
            z += this.SIZE;
        }
        if (x < 0) {
            x += this.SIZE;
        }
        ChunkLoc loc = new ChunkLoc(x, z);
        if (!this.G_SCH.containsKey(loc)) {
            this.G_SCH.put(loc, new HashMap<Short, Short>());
        }
        
        this.G_SCH.get(loc).put(y, id);
        if (isRotate(id)) {
            if (rotate) {
                data = (byte) ((data + 2) % 4);
            }
        }
        else if (data == 0) {
            return;
        }
        if (!this.G_SCH_DATA.containsKey(loc)) {
            this.G_SCH_DATA.put(loc, new HashMap<Short, Byte>());
        }
        
        this.G_SCH_DATA.get(loc).put(y, data);
    }
}
