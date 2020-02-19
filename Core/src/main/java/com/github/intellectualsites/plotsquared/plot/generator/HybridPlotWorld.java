package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.schematic.Schematic;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class HybridPlotWorld extends ClassicPlotWorld {

    private static AffineTransform transform = new AffineTransform().rotateY(90);
    public boolean ROAD_SCHEMATIC_ENABLED;
    public boolean PLOT_SCHEMATIC = false;
    public int PLOT_SCHEMATIC_HEIGHT = -1;
    public short PATH_WIDTH_LOWER;
    public short PATH_WIDTH_UPPER;
    public HashMap<Integer, BaseBlock[]> G_SCH;
    public int SCHEM_Y;
    private Location SIGN_LOCATION;

    public HybridPlotWorld(String worldName, String id, @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
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

    // FIXME depends on block ids
    // Possibly make abstract?
    public static BaseBlock rotate(BaseBlock id) {

        CompoundTag tag = id.getNbtData();

        if (tag != null) {
            // Handle blocks which store their rotation in NBT
            if (tag.containsKey("Rot")) {
                int rot = tag.asInt("Rot");

                Direction direction = MCDirections.fromRotation(rot);

                if (direction != null) {
                    Vector3 vector = transform.apply(direction.toVector())
                        .subtract(transform.apply(Vector3.ZERO)).normalize();
                    Direction newDirection = Direction.findClosest(vector,
                        Direction.Flag.CARDINAL | Direction.Flag.ORDINAL
                            | Direction.Flag.SECONDARY_ORDINAL);

                    if (newDirection != null) {
                        CompoundTagBuilder builder = tag.createBuilder();

                        builder.putByte("Rot", (byte) MCDirections.toRotation(newDirection));

                        id.setNbtData(builder.build());
                    }
                }
            }
        }
        return BlockTransformExtent.transform(id, transform);
    }

    @NotNull @Override protected PlotManager createManager() {
        return new HybridPlotManager(this);
    }

    public Location getSignLocation(Plot plot) {
        plot = plot.getBasePlot(false);
        Location bot = plot.getBottomAbs();
        if (SIGN_LOCATION == null) {
            bot.setY(ROAD_HEIGHT + 1);
            return bot.add(-1, 0, -2);
        } else {
            bot.setY(0);
            return bot.add(SIGN_LOCATION.getX(), SIGN_LOCATION.getY(), SIGN_LOCATION.getZ());
        }
    }

    /**
     * <p>This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.</p>
     */
    @Override public void loadConfiguration(ConfigurationSection config) {
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
        } catch (Exception event) {
            event.printStackTrace();
            PlotSquared.debug("&c - road schematics are disabled for this world.");
        }
    }

    @Override public boolean isCompatible(PlotArea plotArea) {
        if (!(plotArea instanceof SquarePlotWorld)) {
            return false;
        }
        return ((SquarePlotWorld) plotArea).PLOT_WIDTH == this.PLOT_WIDTH;
    }

    public void setupSchematics() throws SchematicHandler.UnsupportedFormatException {
        this.G_SCH = new HashMap<>();
        File root = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(),
            "schematics/GEN_ROAD_SCHEMATIC/" + this.worldname);
        File schematic1File = new File(root, "sideroad.schem");
        if (!schematic1File.exists())
            schematic1File = new File(root, "sideroad.schematic");
        File schematic2File = new File(root, "intersection.schem");
        if (!schematic2File.exists())
            schematic2File = new File(root, "intersection.schematic");
        File schematic3File = new File(root, "plot.schem");
        if (!schematic3File.exists())
            schematic3File = new File(root, "plot.schematic");
        Schematic schematic1 = SchematicHandler.manager.getSchematic(schematic1File);
        Schematic schematic2 = SchematicHandler.manager.getSchematic(schematic2File);
        Schematic schematic3 = SchematicHandler.manager.getSchematic(schematic3File);
        int shift = this.ROAD_WIDTH / 2;
        int oddshift = (this.ROAD_WIDTH & 1) == 0 ? 0 : 1;

        SCHEM_Y = Math.min(PLOT_HEIGHT, ROAD_HEIGHT);
        int plotY = PLOT_HEIGHT - SCHEM_Y;
        int roadY = ROAD_HEIGHT - SCHEM_Y;

        if (schematic3 != null && schematic3.getClipboard().getDimensions().getY() == 256) {
            SCHEM_Y = 0;
            plotY = 0;
            roadY = ROAD_HEIGHT;
        }

        if (schematic1 != null && schematic1.getClipboard().getDimensions().getY() == 256) {
            SCHEM_Y = 0;
            if (schematic3 != null && schematic3.getClipboard().getDimensions().getY() != 256) {
                plotY = PLOT_HEIGHT;
            }
            roadY = 0;
        }

        if (schematic3 != null) {
            this.PLOT_SCHEMATIC = true;
            Clipboard blockArrayClipboard3 = schematic3.getClipboard();

            BlockVector3 d3 = blockArrayClipboard3.getDimensions();
            short w3 = (short) d3.getX();
            short l3 = (short) d3.getZ();
            short h3 = (short) d3.getY();
            if (w3 > PLOT_WIDTH || h3 > PLOT_WIDTH) {
                this.ROAD_SCHEMATIC_ENABLED = true;
            }
            int centerShiftZ;
            if (l3 < this.PLOT_WIDTH) {
                centerShiftZ = (this.PLOT_WIDTH - l3) / 2;
            } else {
                centerShiftZ = (PLOT_WIDTH - l3) / 2;
            }
            int centerShiftX;
            if (w3 < this.PLOT_WIDTH) {
                centerShiftX = (this.PLOT_WIDTH - w3) / 2;
            } else {
                centerShiftX = (PLOT_WIDTH - w3) / 2;
            }

            BlockVector3 min = blockArrayClipboard3.getMinimumPoint();
            for (short x = 0; x < w3; x++) {
                for (short z = 0; z < l3; z++) {
                    for (short y = 0; y < h3; y++) {
                        BaseBlock id = blockArrayClipboard3.getFullBlock(BlockVector3
                            .at(x + min.getBlockX(), y + min.getBlockY(), z + min.getBlockZ()));
                        if (!id.getBlockType().getMaterial().isAir()) {
                            addOverlayBlock((short) (x + shift + oddshift + centerShiftX),
                                (short) (y + plotY), (short) (z + shift + oddshift + centerShiftZ),
                                id, false, h3);
                        }
                    }
                }
            }
/*            HashMap<BlockLoc, CompoundTag> items = schematic3.getTiles();
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
                        if (ln == null || ln.length() > 11)
                            continue outer;
                    }
                    SIGN_LOCATION =
                        new Location(worldname, loc.x + centerShiftX, this.PLOT_HEIGHT + loc.y,
                            loc.z + centerShiftZ);
                    ALLOW_SIGNS = true;
                    continue outer;
                }
            }*/
        }
        if (schematic1 == null || schematic2 == null || this.ROAD_WIDTH == 0) {
            PlotSquared.debug(Captions.PREFIX + "&3 - schematic: &7false");
            return;
        }
        this.ROAD_SCHEMATIC_ENABLED = true;
        // Do not populate road if using schematic population
        // TODO: What? this.ROAD_BLOCK = BlockBucket.empty(); // BlockState.getEmptyData(this.ROAD_BLOCK); // BlockUtil.get(this.ROAD_BLOCK.id, (byte) 0);

        Clipboard blockArrayClipboard1 = schematic1.getClipboard();

        BlockVector3 d1 = blockArrayClipboard1.getDimensions();
        short w1 = (short) d1.getX();
        short l1 = (short) d1.getZ();
        short h1 = (short) d1.getY();

        BlockVector3 min = blockArrayClipboard1.getMinimumPoint();
        for (short x = 0; x < w1; x++) {
            for (short z = 0; z < l1; z++) {
                for (short y = 0; y < h1; y++) {
                    BaseBlock id = blockArrayClipboard1.getFullBlock(BlockVector3
                        .at(x + min.getBlockX(), y + min.getBlockY(), z + min.getBlockZ()));
                    if (!id.getBlockType().getMaterial().isAir()) {
                        addOverlayBlock((short) (x - shift), (short) (y + roadY),
                            (short) (z + shift + oddshift), id, false, h1);
                        addOverlayBlock((short) (z + shift + oddshift), (short) (y + roadY),
                            (short) (shift - x + (oddshift - 1)), id, true, h1);
                    }
                }
            }
        }

        Clipboard blockArrayClipboard2 = schematic2.getClipboard();
        BlockVector3 d2 = blockArrayClipboard2.getDimensions();
        short w2 = (short) d2.getX();
        short l2 = (short) d2.getZ();
        short h2 = (short) d2.getY();
        min = blockArrayClipboard2.getMinimumPoint();
        for (short x = 0; x < w2; x++) {
            for (short z = 0; z < l2; z++) {
                for (short y = 0; y < h2; y++) {
                    BaseBlock id = blockArrayClipboard2.getFullBlock(BlockVector3
                        .at(x + min.getBlockX(), y + min.getBlockY(), z + min.getBlockZ()));
                    if (!id.getBlockType().getMaterial().isAir()) {
                        addOverlayBlock((short) (x - shift), (short) (y + roadY),
                            (short) (z - shift), id, false, h2);
                    }
                }
            }
        }
    }

    public void addOverlayBlock(short x, short y, short z, BaseBlock id, boolean rotate,
        int height) {
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
            id = rotate(id);
        }
        int pair = MathMan.pair(x, z);
        BaseBlock[] existing = this.G_SCH.computeIfAbsent(pair, k -> new BaseBlock[height]);
        if (y >= height) {
            PlotSquared.log("Error adding overlay block. `y > height` ");
            return;
        }
        existing[y] = id;
    }
}
