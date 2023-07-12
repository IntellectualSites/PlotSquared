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
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SingleWorldGenerator extends IndependentPlotGenerator {

    private static final Location bedrock1 = Location.at("", 0, 0, 0);
    private static final Location bedrock2 = Location.at("", 15, 0, 15);
    private static final Location dirt1 = Location.at("", 0, 1, 0);
    private static final Location dirt2 = Location.at("", 15, 2, 15);
    private static final Location grass1 = Location.at("", 0, 3, 0);
    private static final Location grass2 = Location.at("", 15, 3, 15);
    private static final BlockState BEDROCK = BlockTypes.BEDROCK.getDefaultState();
    private static final BlockState DIRT = BlockTypes.DIRT.getDefaultState();
    private static final BlockState GRASS_BLOCK = BlockTypes.GRASS_BLOCK.getDefaultState();

    private final PlotAreaManager plotAreaManager;

    @Inject
    public SingleWorldGenerator(final @NonNull PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
    }

    @Override
    public String getName() {
        return "PlotSquared:single";
    }

    @Override
    public void generateChunk(ZeroedDelegateScopedQueueCoordinator result, PlotArea settings, boolean biomes) {
        SinglePlotArea area = (SinglePlotArea) settings;
        if (area.VOID) {
            Location min = result.getMin();
            if (min.getX() == 0 && min.getZ() == 0) {
                result.setBlock(0, 0, 0, BEDROCK);
            }
        } else {
            result.setCuboid(bedrock1, bedrock2, BEDROCK);
            result.setCuboid(dirt1, dirt2, DIRT);
            result.setCuboid(grass1, grass2, GRASS_BLOCK);
        }
        if (biomes) {
            result.fillBiome(BiomeTypes.PLAINS);
        }
    }

    @Override
    public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return ((SinglePlotAreaManager) this.plotAreaManager).getArea();
    }

    @Override
    public void initialize(PlotArea area) {
    }

    @Override
    public BiomeType getBiome(final PlotArea settings, final int x, final int y, final int z) {
        return BiomeTypes.PLAINS;
    }

}
