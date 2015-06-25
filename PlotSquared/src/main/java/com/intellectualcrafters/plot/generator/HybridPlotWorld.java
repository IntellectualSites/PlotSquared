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
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
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

    public HashMap<PlotLoc, HashMap<Short, Short>> G_SCH;
    public HashMap<PlotLoc, HashMap<Short, Byte>> G_SCH_DATA;
    public HashMap<PlotLoc, HashSet<PlotItem>> G_SCH_STATE;

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
            this.PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2) - 1);
        } else {
            this.PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2));
        }
        this.PATH_WIDTH_UPPER = (short) (this.PATH_WIDTH_LOWER + this.PLOT_WIDTH + 1);
        try {
            setupSchematics();
        } catch (final Exception e) {
            PlotSquared.log("&c - road schematics are disabled for this world.");
        }
    }

    public void setupSchematics() {
        this.G_SCH_DATA = new HashMap<>();
        this.G_SCH = new HashMap<>();
        final String schem1Str = "GEN_ROAD_SCHEMATIC/" + this.worldname + "/sideroad";
        final String schem2Str = "GEN_ROAD_SCHEMATIC/" + this.worldname + "/intersection";
        final String schem3Str = "GEN_ROAD_SCHEMATIC/" + this.worldname + "/plot";
        final Schematic schem1 = SchematicHandler.manager.getSchematic(schem1Str);
        final Schematic schem2 = SchematicHandler.manager.getSchematic(schem2Str);
        final Schematic schem3 = SchematicHandler.manager.getSchematic(schem3Str);
        final int shift = (int) Math.floor(this.ROAD_WIDTH / 2);
        int oddshift = 0;
        if ((this.ROAD_WIDTH % 2) != 0) {
            oddshift = 1;
        }
        if (schem3 != null) {
            this.PLOT_SCHEMATIC = true;
            final DataCollection[] blocks3 = schem3.getBlockCollection();
            final Dimension d3 = schem3.getSchematicDimension();
            final short w3 = (short) d3.getX();
            final short l3 = (short) d3.getZ();
            final short h3 = (short) d3.getY();
            int center_shift_x = 0;
            int center_shift_z = 0;
            if (l3 < this.PLOT_WIDTH) {
                center_shift_z = (this.PLOT_WIDTH - l3) / 2;
            }
            if (w3 < this.PLOT_WIDTH) {
                center_shift_x = (this.PLOT_WIDTH - w3) / 2;
            }
            for (short x = 0; x < w3; x++) {
                for (short z = 0; z < l3; z++) {
                    for (short y = 0; y < h3; y++) {
                        final int index = (y * w3 * l3) + (z * w3) + x;
                        final short id = blocks3[index].getBlock();
                        final byte data = blocks3[index].getData();
                        if (id != 0) {
                            addOverlayBlock((short) (x + shift + oddshift + center_shift_x), (y), (short) (z + shift + oddshift + center_shift_z), id, data, false);
                        }
                    }
                }
            }
            HashSet<PlotItem> items = schem3.getItems();
            if (items != null) {
                G_SCH_STATE = new HashMap<>();
                for (PlotItem item : items) {
                    item.x += shift + oddshift + center_shift_x;
                    item.z += shift + oddshift + center_shift_z;
                    item.y += this.PLOT_HEIGHT;
                    int x = item.x;
                    int y = item.y;
                    int z = item.z;
                    PlotLoc loc = new PlotLoc(x, z);
                    if (!G_SCH_STATE.containsKey(loc)) {
                        G_SCH_STATE.put(loc, new HashSet<PlotItem>());
                    }
                    G_SCH_STATE.get(loc).add(item);
                }
            }
        }
        if ((schem1 == null) || (schem2 == null) || (this.ROAD_WIDTH == 0)) {
            PlotSquared.log(C.PREFIX.s() + "&3 - schematic: &7false");
            return;
        }
        this.ROAD_SCHEMATIC_ENABLED = true;
        // Do not populate road if using schematic population
        this.ROAD_BLOCK = new PlotBlock(this.ROAD_BLOCK.id, (byte) 0);
        final DataCollection[] blocks1 = schem1.getBlockCollection();
        final DataCollection[] blocks2 = schem2.getBlockCollection();
        final Dimension d1 = schem1.getSchematicDimension();
        final short w1 = (short) d1.getX();
        final short l1 = (short) d1.getZ();
        final short h1 = (short) d1.getY();
        final Dimension d2 = schem2.getSchematicDimension();
        final short w2 = (short) d2.getX();
        final short l2 = (short) d2.getZ();
        final short h2 = (short) d2.getY();
        this.SCHEMATIC_HEIGHT = (short) Math.max(h2, h1);
        for (short x = 0; x < w1; x++) {
            for (short z = 0; z < l1; z++) {
                for (short y = 0; y < h1; y++) {
                    final int index = (y * w1 * l1) + (z * w1) + x;
                    final short id = blocks1[index].getBlock();
                    final byte data = blocks1[index].getData();
                    if (id != 0) {
                        addOverlayBlock((short) (x - (shift)), (y), (short) (z + shift + oddshift), id, data, false);
                        addOverlayBlock((short) (z + shift + oddshift), (y), (short) (x - shift), id, data, true);
                    }
                }
            }
        }
        for (short x = 0; x < w2; x++) {
            for (short z = 0; z < l2; z++) {
                for (short y = 0; y < h2; y++) {
                    final int index = (y * w2 * l2) + (z * w2) + x;
                    final short id = blocks2[index].getBlock();
                    final byte data = blocks2[index].getData();
                    if (id != 0) {
                        addOverlayBlock((short) (x - shift), (y), (short) (z - shift), id, data, false);
                    }
                }
            }
        }
    }
    
    public static byte wrap(byte data, int start) {
        if (data >= start && data < start + 4) {
            data = (byte) ((((data - start) + 2) % 4) + start);
        }
        return data;
    }
    
    public static byte wrap2(byte data, int start) {
        if (data >= start && data < start + 2) {
            data = (byte) ((((data - start) + 1) % 2) + start);
        }
        return data;
    }

    public static byte rotate(final short id, byte data) {
        switch (id) {
            case 162:
            case 17: {
                if (data >= 4 && data < 12) {
                    if (data >= 8) {
                        return (byte) (data - 4);
                    }
                    return (byte) (data + 4);
                }
                return data;
            }
            case 183:
            case 184:
            case 185:
            case 186:
            case 187:
            case 107:
            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
            case 128:
            case 134:
            case 135:
            case 136:
            case 156:
            case 163:
            case 164:
            case 180: {
                data = wrap(data, 0);
                data = wrap(data, 4);
                return data;
            }
            
            case 26:
            case 86: {
                data = wrap(data, 0);
                return data;
            }
            case 64:
            case 71:
            case 193:
            case 194:
            case 195:
            case 196:
            case 197:
            case 93:
            case 94:
            case 131:
            case 145:
            case 149:
            case 150:
            case 96:
            case 167: {
                data = wrap(data, 0);
                data = wrap(data, 4);
                data = wrap(data, 8);
                data = wrap(data, 12);
                return data;
            }
            case 28:
            case 66:
            case 157:
            case 27: {
                data = wrap2(data, 0);
                data = wrap2(data, 3);
                if (data == 2) {
                    data = 5;
                }
                else if (data == 5) {
                    data = 2;
                }
                return data;
            }
            
            case 23:
            case 29:
            case 33:
            case 158:
            case 54:
            case 130:
            case 146:
            case 61:
            case 62:
            case 65:
            case 68:
            case 144: {
                data = wrap(data, 2);
                return data;
            }
            case 143:
            case 77: {
                data = wrap(data, 1);
                return data;
            }
            default:
                return data;
        }
    }

    public void addOverlayBlock(short x, final short y, short z, final short id, byte data, final boolean rotate) {
        if (z < 0) {
            z += this.SIZE;
        }
        if (x < 0) {
            x += this.SIZE;
        }
        final PlotLoc loc = new PlotLoc(x, z);
        if (!this.G_SCH.containsKey(loc)) {
            this.G_SCH.put(loc, new HashMap<Short, Short>());
        }
        this.G_SCH.get(loc).put(y, id);
        if (rotate) {
            byte newdata = rotate(id, data);
            if (data == 0 && newdata == 0) {
                return;
            }
            data = newdata;
        }
        if (!this.G_SCH_DATA.containsKey(loc)) {
            this.G_SCH_DATA.put(loc, new HashMap<Short, Byte>());
        }
        this.G_SCH_DATA.get(loc).put(y, data);
    }
}
