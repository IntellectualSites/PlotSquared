/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SingleWorldGenerator extends IndependentPlotGenerator {
    private Location bedrock1 = new Location(null, 0, 0, 0);
    private Location bedrock2 = new Location(null, 15, 0, 15);
    private Location dirt1 = new Location(null, 0, 1, 0);
    private Location dirt2 = new Location(null, 15, 2, 15);
    private Location grass1 = new Location(null, 0, 3, 0);
    private Location grass2 = new Location(null, 15, 3, 15);

    @Override public String getName() {
        return "PlotSquared:single";
    }

    @Override public void generateChunk(ScopedLocalBlockQueue result, PlotArea settings) {
        SinglePlotArea area = (SinglePlotArea) settings;
        if (area.VOID) {
            Location min = result.getMin();
            if (min.getX() == 0 && min.getZ() == 0) {
                result.setBlock(0, 0, 0, BlockTypes.BEDROCK.getDefaultState());
            }
        } else {
            result.setCuboid(bedrock1, bedrock2, BlockTypes.BEDROCK.getDefaultState());
            result.setCuboid(dirt1, dirt2, BlockTypes.DIRT.getDefaultState());
            result.setCuboid(grass1, grass2, BlockTypes.GRASS_BLOCK.getDefaultState());
        }
        result.fillBiome(BiomeTypes.PLAINS);
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return ((SinglePlotAreaManager) PlotSquared.get().getPlotAreaManager()).getArea();
    }

    @Override public void initialize(PlotArea area) {

    }
}
