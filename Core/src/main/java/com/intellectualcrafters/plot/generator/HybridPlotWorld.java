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

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class HybridPlotWorld extends ClassicPlotWorld {

    public boolean ROAD_SCHEMATIC_ENABLED;
    public short SCHEMATIC_HEIGHT;
    public boolean PLOT_SCHEMATIC = false;
    public short REQUIRED_CHANGES = 0;
    public short PATH_WIDTH_LOWER;
    public short PATH_WIDTH_UPPER;
    public HashMap<Integer, HashMap<Integer, PlotBlock>> G_SCH;
    public HashMap<Integer, HashSet<PlotItem>> G_SCH_STATE;

    public HybridPlotWorld(String worldname, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldname, id, generator, min, max);
    }

    public static byte wrap(byte data, int start) {
        if ((data >= start) && (data < (start + 4))) {
            data = (byte) ((((data - start) + 2) & 3) + start);
        }
        return data;
    }

    public static byte wrap2(byte data, int start) {
        if ((data >= start) && (data < (start + 2))) {
            data = (byte) ((((data - start) + 1) & 1) + start);
        }
        return data;
    }

    // FIXME depends on block ids
    // Possibly make abstract?
    public static byte rotate(short id, byte data) {
        switch (id) {
            case 162:
            case 17:
                if (data >= 4 && data < 12) {
                    if (data >= 8) {
                        return (byte) (data - 4);
                    }
                    return (byte) (data + 4);
                }
                return data;
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
            case 180:
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
            case 167:
                data = wrap(data, 0);
                data = wrap(data, 4);
                data = wrap(data, 8);
                data = wrap(data, 12);
                return data;

            case 26:
            case 86:
                data = wrap(data, 0);
                return data;
            case 28:
            case 66:
            case 157:
            case 27:
                data = wrap2(data, 0);
                data = wrap2(data, 3);
                if (data == 2) {
                    data = 5;
                } else if (data == 5) {
                    data = 2;
                }
                return data;

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
            case 144:
                data = wrap(data, 2);
                return data;
            case 143:
            case 77:
                data = wrap(data, 1);
                return data;
            default:
                return data;
        }
    }

    /**
     * <p>This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.</p>
     */
    @Override
    public void loadConfiguration(ConfigurationSection config) {
        super.loadConfiguration(config);
        if ((this.ROAD_WIDTH & 1) == 0) {
            this.PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2) - 1);
        } else {
            this.PATH_WIDTH_LOWER = (short) Math.floor(this.ROAD_WIDTH / 2);
        }
        if (this.ROAD_WIDTH == 0) {
            this.PATH_WIDTH_UPPER = (short) (this.SIZE + 1);
        } else {
            this.PATH_WIDTH_UPPER = (short) (this.PATH_WIDTH_LOWER + this.PLOT_WIDTH + 1);
        }
        try {
            setupSchematics();
        } catch (Exception e) {
            PS.debug("&c - road schematics are disabled for this world.");
        }
    }

    @Override
    public boolean isCompatible(PlotArea plotworld) {
        if (!(plotworld instanceof SquarePlotWorld)) {
            return false;
        }
        return ((SquarePlotWorld) plotworld).PLOT_WIDTH == this.PLOT_WIDTH;
    }

    public void setupSchematics() {
        this.G_SCH = new HashMap<>();
        File schem1File = MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/sideroad.schematic");
        File schem2File =
                MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/intersection.schematic");
        File schem3File = MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/plot.schematic");
        Schematic schem1 = SchematicHandler.manager.getSchematic(schem1File);
        Schematic schem2 = SchematicHandler.manager.getSchematic(schem2File);
        Schematic schem3 = SchematicHandler.manager.getSchematic(schem3File);
        int shift = this.ROAD_WIDTH / 2;
        int oddshift = 0;
        if ((this.ROAD_WIDTH & 1) != 0) {
            oddshift = 1;
        }
        if (schem3 != null) {
            this.PLOT_SCHEMATIC = true;
            short[] ids = schem3.getIds();
            byte[] datas = schem3.getDatas();
            Dimension d3 = schem3.getSchematicDimension();
            short w3 = (short) d3.getX();
            short l3 = (short) d3.getZ();
            short h3 = (short) d3.getY();
            int centerShiftZ = 0;
            if (l3 < this.PLOT_WIDTH) {
                centerShiftZ = (this.PLOT_WIDTH - l3) / 2;
            }
            int centerShiftX = 0;
            if (w3 < this.PLOT_WIDTH) {
                centerShiftX = (this.PLOT_WIDTH - w3) / 2;
            }
            for (short x = 0; x < w3; x++) {
                for (short z = 0; z < l3; z++) {
                    for (short y = 0; y < h3; y++) {
                        int index = (y * w3 * l3) + (z * w3) + x;
                        short id = ids[index];
                        byte data = datas[index];
                        if (id != 0) {
                            addOverlayBlock((short) (x + shift + oddshift + centerShiftX), (short) (y + this.PLOT_HEIGHT),
                                    (short) (z + shift + oddshift + centerShiftZ), id,
                                    data, false);
                        }
                    }
                }
            }
            HashSet<PlotItem> items = schem3.getItems();
            if (items != null) {
                this.G_SCH_STATE = new HashMap<>();
                for (PlotItem item : items) {
                    item.x += shift + oddshift + centerShiftX;
                    item.z += shift + oddshift + centerShiftZ;
                    item.y += this.PLOT_HEIGHT;
                    short x = (short) item.x;
                    short z = (short) item.z;
                    int pair = MathMan.pair(x, z);


                    HashSet<PlotItem> existing = this.G_SCH_STATE.get(pair);
                    if (existing == null) {
                        existing = new HashSet<>();
                        this.G_SCH_STATE.put(pair, existing);
                    }
                    existing.add(item);
                }
            }
        }
        if (schem1 == null || schem2 == null || this.ROAD_WIDTH == 0) {
            PS.debug(C.PREFIX + "&3 - schematic: &7false");
            return;
        }
        this.ROAD_SCHEMATIC_ENABLED = true;
        // Do not populate road if using schematic population
        this.ROAD_BLOCK = new PlotBlock(this.ROAD_BLOCK.id, (byte) 0);

        short[] ids1 = schem1.getIds();
        byte[] datas1 = schem1.getDatas();

        short[] ids2 = schem2.getIds();
        byte[] datas2 = schem2.getDatas();

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
                    int index = (y * w1 * l1) + (z * w1) + x;
                    short id = ids1[index];
                    byte data = datas1[index];
                    if (id != 0) {
                        addOverlayBlock((short) (x - shift), (short) (y + this.ROAD_HEIGHT), (short) (z + shift + oddshift), id, data, false);
                        addOverlayBlock((short) (z + shift + oddshift), (short) (y + this.ROAD_HEIGHT), (short) (x - shift), id, data, true);
                    }
                }
            }
        }
        for (short x = 0; x < w2; x++) {
            for (short z = 0; z < l2; z++) {
                for (short y = 0; y < h2; y++) {
                    int index = (y * w2 * l2) + (z * w2) + x;
                    short id = ids2[index];
                    byte data = datas2[index];
                    if (id != 0) {
                        addOverlayBlock((short) (x - shift), (short) (y + this.ROAD_HEIGHT), (short) (z - shift), id, data, false);
                    }
                }
            }
        }
    }

    public void addOverlayBlock(short x, short y, short z, short id, byte data, boolean rotate) {
        if (z < 0) {
            z += this.SIZE;
        }
        if (x < 0) {
            x += this.SIZE;
        }
        if (rotate) {
            byte newdata = rotate(id, data);
            if (data != 0 || newdata != 0) {
                data = newdata;
            }
        }
        int pair = MathMan.pair(x, z);
        HashMap<Integer, PlotBlock> existing = this.G_SCH.get(pair);
        if (existing == null) {
            existing = new HashMap<>();
            this.G_SCH.put(pair, existing);
        }
        existing.put((int) y, new PlotBlock(id, data));
    }
}
