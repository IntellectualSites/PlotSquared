/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intellectualsites.annotations.DoNotUse;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.SchematicHandler;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HybridPlotWorld extends ClassicPlotWorld {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + HybridPlotWorld.class.getSimpleName());
    private static final AffineTransform transform = new AffineTransform().rotateY(90);
    public boolean ROAD_SCHEMATIC_ENABLED;
    public boolean PLOT_SCHEMATIC = false;
    public short PATH_WIDTH_LOWER;
    public short PATH_WIDTH_UPPER;
    public HashMap<Integer, BaseBlock[]> G_SCH;
    public HashMap<Integer, BiomeType> G_SCH_B;
    /**
     * The Y level at which schematic generation will start, lowest of either road or plot schematic generation.
     */
    public int SCHEM_Y;

    private int plotY;
    private int roadY;
    private Location SIGN_LOCATION;
    private File root = null;
    private int lastOverlayHeightError = Integer.MIN_VALUE;
    private List<Entity> schem3Entities = null;
    private BlockVector3 schem3MinPoint = null;
    private boolean schem1PopulationNeeded = false;
    private boolean schem2PopulationNeeded = false;
    private boolean schem3PopulationNeeded = false;

    @Inject
    private SchematicHandler schematicHandler;

    @Inject
    public HybridPlotWorld(
            @Assisted("world") final String worldName,
            @javax.annotation.Nullable @Assisted("id") final String id,
            @Assisted final @NonNull IndependentPlotGenerator generator,
            @javax.annotation.Nullable @Assisted("min") final PlotId min,
            @javax.annotation.Nullable @Assisted("max") final PlotId max,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        super(worldName, id, generator, min, max, worldConfiguration, blockQueue);
        PlotSquared.platform().injector().injectMembers(this);
    }

    public static BaseBlock rotate(BaseBlock id) {

        CompoundTag tag = id.getNbtData();

        if (tag != null) {
            // Handle blocks which store their rotation in NBT
            if (tag.containsKey("Rot")) {
                int rot = tag.asInt("Rot");

                Direction direction = MCDirections.fromRotation(rot);

                if (direction != null) {
                    Vector3 vector = transform.apply(direction.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                    Direction newDirection =
                            Direction.findClosest(
                                    vector,
                                    Direction.Flag.CARDINAL | Direction.Flag.ORDINAL | Direction.Flag.SECONDARY_ORDINAL
                            );

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

    @NonNull
    @Override
    protected PlotManager createManager() {
        return new HybridPlotManager(
                this, PlotSquared.platform().regionManager(),
                PlotSquared.platform().injector().getInstance(ProgressSubscriberFactory.class)
        );
    }

    public Location getSignLocation(@NonNull Plot plot) {
        plot = plot.getBasePlot(false);
        final Location bot = plot.getBottomAbs();
        if (SIGN_LOCATION == null) {
            return bot.withY(ROAD_HEIGHT + 1).add(-1, 0, -2);
        } else {
            return bot.withY(0).add(SIGN_LOCATION.getX(), SIGN_LOCATION.getY(), SIGN_LOCATION.getZ());
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
            this.PATH_WIDTH_LOWER = (short) (Math.floor(this.ROAD_WIDTH / 2f) - 1);
        } else {
            this.PATH_WIDTH_LOWER = (short) Math.floor(this.ROAD_WIDTH / 2f);
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
        }

        // Dump world settings
        if (Settings.DEBUG) {
            LOGGER.info("- Dumping settings for ClassicPlotWorld with name {}", this.getWorldName());
            final Field[] fields = this.getClass().getFields();
            for (final Field field : fields) {
                final String name = field.getName().toLowerCase(Locale.ENGLISH);
                if (name.contains("g_sch")) {
                    continue;
                }
                Object value;
                try {
                    final boolean accessible = field.canAccess(this);
                    field.setAccessible(true);
                    value = field.get(this);
                    field.setAccessible(accessible);
                } catch (final IllegalAccessException e) {
                    value = String.format("Failed to parse: %s", e.getMessage());
                }
                LOGGER.info("-- {} = {}", name, value);
            }
        }
    }

    @Override
    public boolean isCompatible(final @NonNull PlotArea plotArea) {
        if (!(plotArea instanceof SquarePlotWorld)) {
            return false;
        }
        return ((SquarePlotWorld) plotArea).PLOT_WIDTH == this.PLOT_WIDTH;
    }

    public void setupSchematics() throws SchematicHandler.UnsupportedFormatException {
        this.G_SCH = new HashMap<>();
        this.G_SCH_B = new HashMap<>();

        // Try to determine root. This means that plot areas can have separate schematic
        // directories
        String schematicFolder = Settings.Paths.USE_SCHEMATICS_PATH_FOR_GEN_SCHEMATICS ? Settings.Paths.SCHEMATICS : "schematics";
        if (!(root =
                FileUtils.getFile(
                        PlotSquared.platform().getDirectory(),
                        schematicFolder + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + this.getWorldName() + File.separator + this.getId()
                ))
                .exists()) {
            root = FileUtils.getFile(
                    PlotSquared.platform().getDirectory(),
                    schematicFolder + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + this.getWorldName()
            );
        }

        File schematic1File = new File(root, "sideroad.schem");
        if (!schematic1File.exists()) {
            schematic1File = new File(root, "sideroad.schematic");
        }
        File schematic2File = new File(root, "intersection.schem");
        if (!schematic2File.exists()) {
            schematic2File = new File(root, "intersection.schematic");
        }
        File schematic3File = new File(root, "plot.schem");
        if (!schematic3File.exists()) {
            schematic3File = new File(root, "plot.schematic");
        }
        Schematic schematic1 = this.schematicHandler.getSchematic(schematic1File);
        Schematic schematic2 = this.schematicHandler.getSchematic(schematic2File);
        Schematic schematic3 = this.schematicHandler.getSchematic(schematic3File);

        // If the plot schematic contains entities, then they need to be populated upon generation.
        if (schematic3 != null && !schematic3.getClipboard().getEntities().isEmpty()) {
            this.schem3Entities = new ArrayList<>(schematic3.getClipboard().getEntities());
            this.schem3MinPoint = schematic3.getClipboard().getMinimumPoint();
            this.schem3PopulationNeeded = true;
        }

        int shift = this.ROAD_WIDTH / 2;
        int oddshift = (this.ROAD_WIDTH & 1);

        SCHEM_Y = schematicStartHeight();

        // plotY and roadY are important to allow plot and/or road schematic "overflow" into each other
        // without causing AIOOB exceptions when attempting either to set blocks to, or get block from G_SCH
        // Default plot schematic start height, normalized to the minimum height schematics are pasted from.
        plotY = PLOT_HEIGHT - SCHEM_Y;
        int minRoadWall = Settings.Schematics.USE_WALL_IN_ROAD_SCHEM_HEIGHT ? Math.min(ROAD_HEIGHT, WALL_HEIGHT) : ROAD_HEIGHT;
        // Default road schematic start height, normalized to the minimum height schematics are pasted from.
        roadY = minRoadWall - SCHEM_Y;

        int worldGenHeight = getMaxGenHeight() - getMinGenHeight() + 1;

        int plotSchemHeight = 0;

        // SCHEM_Y should be normalised to the plot "start" height
        if (schematic3 != null) {
            plotSchemHeight = schematic3.getClipboard().getDimensions().getY();
            if (plotSchemHeight == worldGenHeight) {
                SCHEM_Y = getMinGenHeight();
                plotY = 0;
            } else if (!Settings.Schematics.PASTE_ON_TOP) {
                SCHEM_Y = getMinGenHeight();
                plotY = 0;
            }
        }

        int roadSchemHeight = 0;

        if (schematic1 != null) {
            roadSchemHeight = Math.max(
                    schematic1.getClipboard().getDimensions().getY(),
                    schematic2.getClipboard().getDimensions().getY()
            );
            if (roadSchemHeight == worldGenHeight) {
                SCHEM_Y = getMinGenHeight();
                roadY = 0; // Road is the lowest schematic
                if (schematic3 != null && schematic3.getClipboard().getDimensions().getY() != worldGenHeight) {
                    // Road is the lowest schematic. Normalize plotY to it.
                    if (Settings.Schematics.PASTE_ON_TOP) {
                        plotY = PLOT_HEIGHT - getMinGenHeight();
                    }
                }
            } else if (!Settings.Schematics.PASTE_ROAD_ON_TOP) {
                roadY = 0;
                SCHEM_Y = getMinGenHeight();
                if (schematic3 != null) {
                    if (Settings.Schematics.PASTE_ON_TOP) {
                        // Road is the lowest schematic. Normalize plotY to it.
                        plotY = PLOT_HEIGHT - SCHEM_Y;
                    }
                }
            } else {
                roadY = minRoadWall - SCHEM_Y;
            }
        }
        int maxSchematicHeight = Math.max(plotY + plotSchemHeight, roadY + roadSchemHeight);

        if (schematic3 != null) {
            this.PLOT_SCHEMATIC = true;
            Clipboard blockArrayClipboard3 = schematic3.getClipboard();

            BlockVector3 d3 = blockArrayClipboard3.getDimensions();
            short w3 = (short) d3.getX();
            short l3 = (short) d3.getZ();
            short h3 = (short) d3.getY();
            if (w3 > PLOT_WIDTH || l3 > PLOT_WIDTH) {
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
                        BaseBlock id = blockArrayClipboard3.getFullBlock(BlockVector3.at(
                                x + min.getBlockX(),
                                y + min.getBlockY(),
                                z + min.getBlockZ()
                        ));
                        schem3PopulationNeeded |= id.hasNbtData();
                        addOverlayBlock(
                                (short) (x + shift + oddshift + centerShiftX),
                                (short) (y + plotY),
                                (short) (z + shift + oddshift + centerShiftZ),
                                id,
                                false,
                                maxSchematicHeight
                        );
                    }
                    if (blockArrayClipboard3.hasBiomes()) {
                        BiomeType biome = blockArrayClipboard3.getBiome(BlockVector2.at(
                                x + min.getBlockX(),
                                z + min.getBlockZ()
                        ));
                        addOverlayBiome(
                                (short) (x + shift + oddshift + centerShiftX),
                                (short) (z + shift + oddshift + centerShiftZ),
                                biome
                        );
                    }
                }
            }

            if (Settings.DEBUG) {
                LOGGER.info("- plot schematic: {}", schematic3File.getPath());
            }
        }
        if ((schematic1 == null && schematic2 == null) || this.ROAD_WIDTH == 0) {
            if (Settings.DEBUG) {
                LOGGER.info("- road schematic: false");
            }
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
        // Workaround for schematic height issue if proper calculation of road schematic height is disabled
        if (!Settings.Schematics.USE_WALL_IN_ROAD_SCHEM_HEIGHT) {
            h1 += Math.max(ROAD_HEIGHT - WALL_HEIGHT, 0);
        }

        BlockVector3 min = blockArrayClipboard1.getMinimumPoint();
        for (short x = 0; x < w1; x++) {
            for (short z = 0; z < l1; z++) {
                for (short y = 0; y < h1; y++) {
                    BaseBlock id = blockArrayClipboard1.getFullBlock(BlockVector3.at(
                            x + min.getBlockX(),
                            y + min.getBlockY(),
                            z + min.getBlockZ()
                    ));
                    schem1PopulationNeeded |= id.hasNbtData();
                    addOverlayBlock(
                            (short) (x - shift),
                            (short) (y + roadY),
                            (short) (z + shift + oddshift),
                            id,
                            false,
                            maxSchematicHeight
                    );
                    addOverlayBlock(
                            (short) (z + shift + oddshift),
                            (short) (y + roadY),
                            (short) (shift - x + (oddshift - 1)),
                            id,
                            true,
                            maxSchematicHeight
                    );
                }
                if (blockArrayClipboard1.hasBiomes()) {
                    BiomeType biome = blockArrayClipboard1.getBiome(BlockVector2.at(x + min.getBlockX(), z + min.getBlockZ()));
                    addOverlayBiome((short) (x - shift), (short) (z + shift + oddshift), biome);
                    addOverlayBiome((short) (z + shift + oddshift), (short) (shift - x + (oddshift - 1)), biome);
                }
            }
        }

        Clipboard blockArrayClipboard2 = schematic2.getClipboard();
        BlockVector3 d2 = blockArrayClipboard2.getDimensions();
        short w2 = (short) d2.getX();
        short l2 = (short) d2.getZ();
        short h2 = (short) d2.getY();
        // Workaround for schematic height issue if proper calculation of road schematic height is disabled
        if (!Settings.Schematics.USE_WALL_IN_ROAD_SCHEM_HEIGHT) {
            h2 += Math.max(ROAD_HEIGHT - WALL_HEIGHT, 0);
        }
        min = blockArrayClipboard2.getMinimumPoint();
        for (short x = 0; x < w2; x++) {
            for (short z = 0; z < l2; z++) {
                for (short y = 0; y < h2; y++) {
                    BaseBlock id = blockArrayClipboard2.getFullBlock(BlockVector3.at(
                            x + min.getBlockX(),
                            y + min.getBlockY(),
                            z + min.getBlockZ()
                    ));
                    schem2PopulationNeeded |= id.hasNbtData();
                    addOverlayBlock(
                            (short) (x - shift),
                            (short) (y + roadY),
                            (short) (z - shift),
                            id,
                            false,
                            maxSchematicHeight
                    );
                }
                if (blockArrayClipboard2.hasBiomes()) {
                    BiomeType biome = blockArrayClipboard2.getBiome(BlockVector2.at(x + min.getBlockX(), z + min.getBlockZ()));
                    addOverlayBiome((short) (x - shift), (short) (z - shift), biome);
                }
            }
        }
    }

    private void addOverlayBlock(short x, short y, short z, BaseBlock id, boolean rotate, int height) {
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
            if (y > lastOverlayHeightError) {
                lastOverlayHeightError = y;
                LOGGER.error(
                        "Error adding overlay block in world {}. `y > height`. y={}, height={}",
                        getWorldName(),
                        y,
                        height
                );
            }
            return;
        }
        existing[y] = id;
    }

    private void addOverlayBiome(short x, short z, BiomeType id) {
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
        int pair = MathMan.pair(x, z);
        this.G_SCH_B.put(pair, id);
    }

    /**
     * Get the entities contained within the plot schematic for generation. Intended for internal use only.
     *
     * @since 6.9.0
     */
    @DoNotUse
    public @Nullable List<Entity> getPlotSchematicEntities() {
        return schem3Entities;
    }

    /**
     * Get the minimum point of the plot schematic for generation. Intended for internal use only.
     *
     * @since 6.9.0
     */
    @DoNotUse
    public @Nullable BlockVector3 getPlotSchematicMinPoint() {
        return schem3MinPoint;
    }

    /**
     * Get if post-generation population of chunks with tiles/entities is needed for this world. Not for public API use.
     *
     * @since 6.9.0
     */
    @DoNotUse
    public boolean populationNeeded() {
        return schem1PopulationNeeded || schem2PopulationNeeded || schem3PopulationNeeded;
    }

    /**
     * Get the root folder for this world's generation schematics. May be null if schematics not initialised via
     * {@link HybridPlotWorld#setupSchematics()}
     *
     * @since 6.9.0
     */
    public @Nullable File getSchematicRoot() {
        return this.root;
    }

    /**
     * Get the y value where the plot schematic should be pasted from.
     *
     * @return plot schematic y start value
     * @since 7.0.0
     */
    public int getPlotYStart() {
        return SCHEM_Y + plotY;
    }

    /**
     * Get the y value where the road schematic should be pasted from.
     *
     * @return road schematic y start value
     * @since 7.0.0
     */
    public int getRoadYStart() {
        return SCHEM_Y + roadY;
    }

}
