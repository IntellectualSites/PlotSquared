package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
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
