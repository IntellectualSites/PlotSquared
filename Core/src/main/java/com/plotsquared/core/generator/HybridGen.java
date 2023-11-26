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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;

public class HybridGen extends IndependentPlotGenerator {

    private static final CuboidRegion CHUNK = new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(15, 396, 15));
    private final HybridPlotWorldFactory hybridPlotWorldFactory;

    @Inject
    public HybridGen(final @NonNull HybridPlotWorldFactory hybridPlotWorldFactory) {
        this.hybridPlotWorldFactory = hybridPlotWorldFactory;
    }

    @Override
    public String getName() {
        return PlotSquared.platform().pluginName();
    }

    private void placeSchem(
            HybridPlotWorld world,
            ZeroedDelegateScopedQueueCoordinator result,
            short relativeX,
            short relativeZ,
            int x,
            int z,
            EnumSet<SchematicFeature> features
    ) {
        int minY; // Math.min(world.PLOT_HEIGHT, world.ROAD_HEIGHT);
        boolean isRoad = features.contains(SchematicFeature.ROAD);
        if ((isRoad && Settings.Schematics.PASTE_ROAD_ON_TOP) || (!isRoad && Settings.Schematics.PASTE_ON_TOP)) {
            minY = world.SCHEM_Y;
        } else {
            minY = world.getMinBuildHeight();
        }
        BaseBlock[] blocks = world.G_SCH.get(MathMan.pair(relativeX, relativeZ));
        if (blocks != null) {
            for (int y = 0; y < blocks.length; y++) {
                if (blocks[y] != null) {
                    if (!features.contains(SchematicFeature.POPULATING) || blocks[y].hasNbtData()) {
                        result.setBlock(x, minY + y, z, blocks[y]);
                    }
                }
            }
        }
        if (!features.contains(SchematicFeature.BIOMES)) {
            return;
        }
        BiomeType biome = world.G_SCH_B.get(MathMan.pair(relativeX, relativeZ));
        if (biome != null) {
            result.setBiome(x, z, biome);
        }
    }

    @Override
    public void generateChunk(@NonNull ZeroedDelegateScopedQueueCoordinator result, @NonNull PlotArea settings, boolean biomes) {
        Preconditions.checkNotNull(result, "result cannot be null");
        Preconditions.checkNotNull(settings, "settings cannot be null");

        HybridPlotWorld hybridPlotWorld = (HybridPlotWorld) settings;
        // Biome
        if (biomes) {
            result.fillBiome(hybridPlotWorld.getPlotBiome());
        }
        // Bedrock
        if (hybridPlotWorld.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    result.setBlock(x, hybridPlotWorld.getMinGenHeight(), z, BlockTypes.BEDROCK.getDefaultState());
                }
            }
        }
        EnumSet<SchematicFeature> roadFeatures = EnumSet.of(SchematicFeature.ROAD);
        EnumSet<SchematicFeature> plotFeatures = EnumSet.noneOf(SchematicFeature.class);
        if (biomes) {
            roadFeatures.add(SchematicFeature.BIOMES);
            plotFeatures.add(SchematicFeature.BIOMES);
        }

        // Coords
        Location min = result.getMin();
        int bx = min.getX() - hybridPlotWorld.ROAD_OFFSET_X;
        int bz = min.getZ() - hybridPlotWorld.ROAD_OFFSET_Z;

        // The relative X-coordinate (within the plot) of the minimum X coordinate
        // contained in the scoped queue
        short relativeOffsetX = (short) Math.floorMod(bx, hybridPlotWorld.SIZE);
        // The relative Z-coordinate (within the plot) of the minimum Z coordinate
        // contained in the scoped queue
        short relativeOffsetZ = (short) Math.floorMod(bz, hybridPlotWorld.SIZE);

        // The X-coordinate of a given X coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeX = new short[16];
        boolean[] insideRoadX = new boolean[16];
        boolean[] insideWallX = new boolean[16];
        short offsetX = relativeOffsetX;
        for (short i = 0; i < 16; i++) {
            if (offsetX >= hybridPlotWorld.SIZE) {
                offsetX -= hybridPlotWorld.SIZE;
            }
            relativeX[i] = offsetX;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                insideRoadX[i] = offsetX < hybridPlotWorld.PATH_WIDTH_LOWER || offsetX > hybridPlotWorld.PATH_WIDTH_UPPER;
                insideWallX[i] = offsetX == hybridPlotWorld.PATH_WIDTH_LOWER || offsetX == hybridPlotWorld.PATH_WIDTH_UPPER;
            }
            offsetX++;
        }
        // The Z-coordinate of a given Z coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeZ = new short[16];
        boolean[] insideRoadZ = new boolean[16];
        boolean[] insideWallZ = new boolean[16];
        short offsetZ = relativeOffsetZ;
        for (short i = 0; i < 16; i++) {
            if (offsetZ >= hybridPlotWorld.SIZE) {
                offsetZ -= hybridPlotWorld.SIZE;
            }
            relativeZ[i] = offsetZ;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                insideRoadZ[i] = offsetZ < hybridPlotWorld.PATH_WIDTH_LOWER || offsetZ > hybridPlotWorld.PATH_WIDTH_UPPER;
                insideWallZ[i] = offsetZ == hybridPlotWorld.PATH_WIDTH_LOWER || offsetZ == hybridPlotWorld.PATH_WIDTH_UPPER;
            }
            offsetZ++;
        }
        // generation
        int startY = hybridPlotWorld.getMinGenHeight() + (hybridPlotWorld.PLOT_BEDROCK ? 1 : 0);
        for (short x = 0; x < 16; x++) {
            if (insideRoadX[x]) {
                for (short z = 0; z < 16; z++) {
                    // Road
                    for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                        result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                    }
                    if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                        placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                    }
                }
            } else if (insideWallX[x]) {
                for (short z = 0; z < 16; z++) {
                    if (insideRoadZ[z]) {
                        // road
                        for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                        }
                        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                        }
                    } else {
                        // wall
                        for (int y = startY; y <= hybridPlotWorld.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.WALL_FILLING.toPattern());
                        }
                        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            if (hybridPlotWorld.PLACE_TOP_BLOCK) {
                                result.setBlock(x, hybridPlotWorld.WALL_HEIGHT + 1, z, hybridPlotWorld.WALL_BLOCK.toPattern());
                            }
                        } else {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                        }
                    }
                }
            } else {
                for (short z = 0; z < 16; z++) {
                    if (insideRoadZ[z]) {
                        // road
                        for (int y = startY; y <= hybridPlotWorld.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.ROAD_BLOCK.toPattern());
                        }
                        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                        }
                    } else if (insideWallZ[z]) {
                        // wall
                        for (int y = startY; y <= hybridPlotWorld.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.WALL_FILLING.toPattern());
                        }
                        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            if (hybridPlotWorld.PLACE_TOP_BLOCK) {
                                result.setBlock(x, hybridPlotWorld.WALL_HEIGHT + 1, z, hybridPlotWorld.WALL_BLOCK.toPattern());
                            }
                        } else {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                        }
                    } else {
                        // plot
                        for (int y = startY; y < hybridPlotWorld.PLOT_HEIGHT; y++) {
                            result.setBlock(x, y, z, hybridPlotWorld.MAIN_BLOCK.toPattern());
                        }
                        result.setBlock(x, hybridPlotWorld.PLOT_HEIGHT, z, hybridPlotWorld.TOP_BLOCK.toPattern());
                        if (hybridPlotWorld.PLOT_SCHEMATIC) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, plotFeatures);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void populateChunk(final ZeroedDelegateScopedQueueCoordinator result, final PlotArea settings) {
        HybridPlotWorld hybridPlotWorld = (HybridPlotWorld) settings;
        if (!hybridPlotWorld.populationNeeded()) {
            return;
        }
        EnumSet<SchematicFeature> roadFeatures = EnumSet.of(SchematicFeature.POPULATING, SchematicFeature.ROAD);
        EnumSet<SchematicFeature> plotFeatures = EnumSet.of(SchematicFeature.POPULATING);

        // Coords
        Location min = result.getMin();
        int bx = min.getX() - hybridPlotWorld.ROAD_OFFSET_X;
        int bz = min.getZ() - hybridPlotWorld.ROAD_OFFSET_Z;

        // The relative X-coordinate (within the plot) of the minimum X coordinate
        // contained in the scoped queue
        short relativeOffsetX = (short) Math.floorMod(bx, hybridPlotWorld.SIZE);
        // The relative Z-coordinate (within the plot) of the minimum Z coordinate
        // contained in the scoped queue
        short relativeOffsetZ = (short) Math.floorMod(bz, hybridPlotWorld.SIZE);

        boolean allRoad = true;
        boolean overlap = false;

        // The X-coordinate of a given X coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeX = new short[16];
        boolean[] insideRoadX = new boolean[16];
        boolean[] insideWallX = new boolean[16];
        short offsetX = relativeOffsetX;
        for (short i = 0; i < 16; i++) {
            if (offsetX >= hybridPlotWorld.SIZE) {
                offsetX -= hybridPlotWorld.SIZE;
                overlap = true;
            }
            relativeX[i] = offsetX;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                boolean insideRoad = offsetX < hybridPlotWorld.PATH_WIDTH_LOWER || offsetX > hybridPlotWorld.PATH_WIDTH_UPPER;
                boolean insideWall = offsetX == hybridPlotWorld.PATH_WIDTH_LOWER || offsetX == hybridPlotWorld.PATH_WIDTH_UPPER;
                insideRoadX[i] = insideRoad;
                insideWallX[i] = insideWall;
                allRoad &= insideRoad && insideWall;
            }
            offsetX++;
        }

        // The Z-coordinate of a given Z coordinate, relative to the
        // plot (Counting from the corner with the least positive
        // coordinates)
        short[] relativeZ = new short[16];
        boolean[] insideRoadZ = new boolean[16];
        boolean[] insideWallZ = new boolean[16];
        short offsetZ = relativeOffsetZ;
        for (short i = 0; i < 16; i++) {
            if (offsetZ >= hybridPlotWorld.SIZE) {
                offsetZ -= hybridPlotWorld.SIZE;
                overlap = true;
            }
            relativeZ[i] = offsetZ;
            if (hybridPlotWorld.ROAD_WIDTH != 0) {
                boolean insideRoad = offsetZ < hybridPlotWorld.PATH_WIDTH_LOWER || offsetZ > hybridPlotWorld.PATH_WIDTH_UPPER;
                boolean insideWall = offsetZ == hybridPlotWorld.PATH_WIDTH_LOWER || offsetZ == hybridPlotWorld.PATH_WIDTH_UPPER;
                insideRoadZ[i] = insideRoad;
                insideWallZ[i] = insideWall;
                allRoad &= insideRoad && insideWall;
            }
            offsetZ++;
        }
        for (short x = 0; x < 16; x++) {
            if (insideRoadX[x] || insideWallX[x]) {
                if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                    for (short z = 0; z < 16; z++) {
                        placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                    }
                }
            } else {
                for (short z = 0; z < 16; z++) {
                    if (insideRoadZ[z] || insideWallZ[z]) {
                        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, roadFeatures);
                        }
                    } else if (hybridPlotWorld.PLOT_SCHEMATIC) {
                        placeSchem(hybridPlotWorld, result, relativeX[x], relativeZ[z], x, z, plotFeatures);
                    }
                }
            }
        }
        if (!allRoad && hybridPlotWorld.getPlotSchematicEntities() != null && !hybridPlotWorld
                .getPlotSchematicEntities()
                .isEmpty()) {
            CuboidRegion region = CHUNK.clone();
            try {
                region.shift(hybridPlotWorld
                        .getPlotSchematicMinPoint()
                        .add(relativeOffsetX, 0, relativeOffsetZ)
                        .subtract(hybridPlotWorld.PATH_WIDTH_LOWER + 1, 0, hybridPlotWorld.PATH_WIDTH_LOWER + 1));
                for (Entity entity : hybridPlotWorld.getPlotSchematicEntities()) {
                    if (region.contains(entity.getLocation().toVector().toBlockPoint())) {
                        Vector3 pos = (entity.getLocation().toVector()
                                .subtract(region
                                        .getMinimumPoint()
                                        .withY(hybridPlotWorld.getPlotSchematicMinPoint().getY())
                                        .toVector3()))
                                .add(min.getBlockVector3().withY(hybridPlotWorld.SCHEM_Y).toVector3());
                        result.setEntity(new PopulatingEntity(
                                entity,
                                new com.sk89q.worldedit.util.Location(NullWorld.getInstance(), pos)
                        ));
                    }
                }
            } catch (RegionOperationException e) {
                throw new RuntimeException(e);
            }
            if (overlap) {
                try {
                    region.shift(BlockVector3.at(-hybridPlotWorld.SIZE, 0, -hybridPlotWorld.SIZE));
                    for (Entity entity : hybridPlotWorld.getPlotSchematicEntities()) {
                        if (region.contains(entity.getLocation().toVector().toBlockPoint())) {
                            result.setEntity(entity);
                        }
                    }
                } catch (RegionOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return;
    }

    @Override
    public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return this.hybridPlotWorldFactory.create(world, id, this, min, max);
    }

    @Override
    public void initialize(PlotArea area) {
        // All initialization is done in the PlotArea class
    }

    @Override
    public BiomeType getBiome(final PlotArea settings, final int worldX, final int worldY, final int worldZ) {
        HybridPlotWorld hybridPlotWorld = (HybridPlotWorld) settings;
        if (!hybridPlotWorld.PLOT_SCHEMATIC && !hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return hybridPlotWorld.getPlotBiome();
        }
        int relativeX = worldX;
        int relativeZ = worldZ;
        if (hybridPlotWorld.ROAD_OFFSET_X != 0) {
            relativeX -= hybridPlotWorld.ROAD_OFFSET_X;
        }
        if (hybridPlotWorld.ROAD_OFFSET_Z != 0) {
            relativeZ -= hybridPlotWorld.ROAD_OFFSET_Z;
        }
        int size = hybridPlotWorld.PLOT_WIDTH + hybridPlotWorld.ROAD_WIDTH;
        relativeX = Math.floorMod(relativeX, size);
        relativeZ = Math.floorMod(relativeZ, size);
        BiomeType biome = hybridPlotWorld.G_SCH_B.get(MathMan.pair((short) relativeX, (short) relativeZ));
        return biome == null ? hybridPlotWorld.getPlotBiome() : biome;
    }

    private enum SchematicFeature {
        BIOMES,
        ROAD,
        POPULATING
    }

    /**
     * Wrapper to allow a WorldEdit {@link Entity} to effectively have a mutable location as the location in its NBT should be changed
     * when set to the world.
     *
     * @since 6.9.0
     */
    private static final class PopulatingEntity implements Entity {

        private final Entity parent;
        private com.sk89q.worldedit.util.Location location;

        /**
         * @since 6.9.0
         */
        private PopulatingEntity(Entity parent, com.sk89q.worldedit.util.Location location) {
            this.parent = parent;
            this.location = location;
        }

        @Nullable
        @Override
        public BaseEntity getState() {
            return parent.getState();
        }

        @Override
        public boolean remove() {
            return parent.remove();
        }

        @Override
        public com.sk89q.worldedit.util.Location getLocation() {
            return location;
        }

        @Override
        public boolean setLocation(final com.sk89q.worldedit.util.Location location) {
            this.location = location;
            return true;
        }

        @Override
        public Extent getExtent() {
            return parent.getExtent();
        }

        @Nullable
        @Override
        public <T> T getFacet(final Class<? extends T> cls) {
            return parent.getFacet(cls);
        }

    }

}
