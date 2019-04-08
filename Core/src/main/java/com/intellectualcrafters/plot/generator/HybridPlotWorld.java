package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HybridPlotWorld extends ClassicPlotWorld {

    public boolean ROAD_SCHEMATIC_ENABLED;
    public boolean PLOT_SCHEMATIC = false;
    public short PATH_WIDTH_LOWER;
    public short PATH_WIDTH_UPPER;
    public HashMap<Integer, char[]> G_SCH;
    public HashMap<Integer, HashMap<Integer, CompoundTag>> G_SCH_STATE;
    private Location SIGN_LOCATION;
    public int SCHEM_Y;

    public HybridPlotWorld(String worldName, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
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

    public Location getSignLocation(Plot plot) {
        plot = plot.getBasePlot(false);
        Location bot = plot.getBottomAbs();
        if (SIGN_LOCATION == null) {
            bot.setY(ROAD_HEIGHT + 1);
            return bot.add(-1, 0, -2);
        } else {
            bot.setY(0);
            Location loc = bot.add(SIGN_LOCATION.getX(), SIGN_LOCATION.getY(), SIGN_LOCATION.getZ());
            return loc;
        }
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
            case 26: // bed
            case 86: // pumpkin
            case 91:
            case 183: // fence gate
            case 184:
            case 185:
            case 186:
            case 187:
            case 107:
                data = wrap2(data, 0);
                data = wrap2(data, 2);
                data = wrap2(data, 4);
                data = wrap2(data, 6);
                return data;
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
        } catch (Exception ignored) {
            PS.debug("&c - road schematics are disabled for this world.");
        }
    }

    @Override
    public boolean isCompatible(PlotArea plotArea) {
        if (!(plotArea instanceof SquarePlotWorld)) {
            return false;
        }
        return ((SquarePlotWorld) plotArea).PLOT_WIDTH == this.PLOT_WIDTH;
    }

    public void setupSchematics() {
        this.G_SCH = new HashMap<>();
        File schematic1File =
                MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/sideroad.schematic");
        File schematic2File =
                MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/intersection.schematic");
        File schem3File = MainUtil.getFile(PS.get().IMP.getDirectory(), "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname + "/plot.schematic");
        Schematic schematic1 = SchematicHandler.manager.getSchematic(schematic1File);
        Schematic schematic2 = SchematicHandler.manager.getSchematic(schematic2File);
        Schematic schematic3 = SchematicHandler.manager.getSchematic(schem3File);
        int shift = this.ROAD_WIDTH / 2;
        int oddshift = (this.ROAD_WIDTH & 1) == 0 ? 0 : 1;

        SCHEM_Y = Math.min(PLOT_HEIGHT, ROAD_HEIGHT);
        int plotY = PLOT_HEIGHT - SCHEM_Y;
        int roadY = ROAD_HEIGHT - SCHEM_Y;

        if (schematic3 != null && schematic3.getSchematicDimension().getY() == 256) {
            SCHEM_Y = 0;
            plotY = 0;
            roadY = ROAD_HEIGHT;
        }

        if (schematic1 != null && schematic1.getSchematicDimension().getY() == 256) {
            SCHEM_Y = 0;
            if (schematic3 != null && schematic3.getSchematicDimension().getY() != 256) {
                plotY = PLOT_HEIGHT;
            }
            roadY = 0;
        }

        if (schematic3 != null) {
            this.PLOT_SCHEMATIC = true;
            short[] ids = schematic3.getIds();
            byte[] datas = schematic3.getDatas();
            Dimension d3 = schematic3.getSchematicDimension();
            short w3 = (short) d3.getX();
            short l3 = (short) d3.getZ();
            short h3 = (short) d3.getY();
            if (w3 > PLOT_WIDTH || h3 > PLOT_WIDTH) {
                this.ROAD_SCHEMATIC_ENABLED = true;
            }
            int centerShiftZ = 0;
            if (l3 < this.PLOT_WIDTH) {
                centerShiftZ = (this.PLOT_WIDTH - l3) / 2;
            } else {
                centerShiftZ = (PLOT_WIDTH - l3) / 2;
            }
            int centerShiftX = 0;
            if (w3 < this.PLOT_WIDTH) {
                centerShiftX = (this.PLOT_WIDTH - w3) / 2;
            } else {
                centerShiftX = (PLOT_WIDTH - w3) / 2;
            }

            for (short x = 0; x < w3; x++) {
                for (short z = 0; z < l3; z++) {
                    for (short y = 0; y < h3; y++) {
                        int index = (y * w3 * l3) + (z * w3) + x;
                        short id = ids[index];
                        byte data = datas[index];
                        if (id != 0) {
                            addOverlayBlock((short) (x + shift + oddshift + centerShiftX), (short) (y + plotY),
                                    (short) (z + shift + oddshift + centerShiftZ), id,
                                    data, false, h3);
                        }
                    }
                }
            }
            HashMap<BlockLoc, CompoundTag> items = schematic3.getTiles();
            if (!items.isEmpty()) {
                this.G_SCH_STATE = new HashMap<>();
                outer:
                for (Map.Entry<BlockLoc, CompoundTag> entry : items.entrySet()) {
                    BlockLoc loc = entry.getKey();
                    short x = (short) (loc.x + shift + oddshift + centerShiftX);
                    short z = (short) (loc.z + shift + oddshift + centerShiftZ);
                    short y = (short) (loc.y + this.PLOT_HEIGHT);
                    int pair = MathMan.pair(x, z);
                    HashMap<Integer, CompoundTag> existing = this.G_SCH_STATE.get(pair);
                    if (existing == null) {
                        existing = new HashMap<>();
                        this.G_SCH_STATE.put(pair, existing);
                    }
                    existing.put((int) y, entry.getValue());

                    CompoundTag tag = entry.getValue();
                    Map<String, Tag> map = ReflectionUtils.getMap(tag.getValue());
                    for (int i = 1; i <= 4; i++) {
                        String ln = tag.getString("Line" + i);
                        if (ln == null || ln.length() > 11) continue outer;
                    }
                    SIGN_LOCATION = new Location(worldname, loc.x + centerShiftX, this.PLOT_HEIGHT + loc.y, loc.z + centerShiftZ);
                    ALLOW_SIGNS = true;
                    continue outer;
                }
            }
        }
        if (schematic1 == null || schematic2 == null || this.ROAD_WIDTH == 0) {
            PS.debug(C.PREFIX + "&3 - schematic: &7false");
            return;
        }
        this.ROAD_SCHEMATIC_ENABLED = true;
        // Do not populate road if using schematic population
        this.ROAD_BLOCK = PlotBlock.get(this.ROAD_BLOCK.id, (byte) 0);

        short[] ids1 = schematic1.getIds();
        byte[] datas1 = schematic1.getDatas();

        short[] ids2 = schematic2.getIds();
        byte[] datas2 = schematic2.getDatas();

        Dimension d1 = schematic1.getSchematicDimension();
        short w1 = (short) d1.getX();
        short l1 = (short) d1.getZ();
        short h1 = (short) d1.getY();
        Dimension d2 = schematic2.getSchematicDimension();
        short w2 = (short) d2.getX();
        short l2 = (short) d2.getZ();
        short h2 = (short) d2.getY();
        for (short x = 0; x < w1; x++) {
            for (short z = 0; z < l1; z++) {
                for (short y = 0; y < h1; y++) {
                    int index = (y * w1 * l1) + (z * w1) + x;
                    short id = ids1[index];
                    byte data = datas1[index];
                    if (id != 0) {
                        addOverlayBlock((short) (x - shift), (short) (y + roadY), (short) (z + shift + oddshift), id, data, false, h1);
                        addOverlayBlock((short) (z + shift + oddshift), (short) (y + roadY), (short) (x - shift), id, data, true, h1);
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
                        addOverlayBlock((short) (x - shift), (short) (y + roadY), (short) (z - shift), id, data, false, h2);
                    }
                }
            }
        }
    }

    public void addOverlayBlock(short x, short y, short z, short id, byte data, boolean rotate, int height) {
        if (z < 0) {
            z += this.SIZE;
        } else if (z >= this.SIZE) {
            z -= this.SIZE;
        }
        if (x < 0) {
            x += this.SIZE;
        } else if (x >= this.SIZE) {
            x -= this.SIZE;
        }
        if (rotate) {
            byte newData = rotate(id, data);
            if (data != 0 || newData != 0) {
                data = newData;
            }
        }
        int pair = MathMan.pair(x, z);
        char[] existing = this.G_SCH.get(pair);
        if (existing == null) {
            existing = new char[height];
            this.G_SCH.put(pair, existing);
        }
        if (id == 0) {
            data = 1;
        }
        existing[y] = (char) ((id << 4) + data);
    }
}
