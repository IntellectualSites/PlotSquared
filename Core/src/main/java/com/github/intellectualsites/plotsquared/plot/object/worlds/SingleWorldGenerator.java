package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;

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

    @Override public void generateChunk(ScopedLocalBlockQueue result, PlotArea settings,
        PseudoRandom random) {
        SinglePlotArea area = (SinglePlotArea) settings;
        if (area.VOID) {
            Location min = result.getMin();
            if (min.getX() == 0 && min.getZ() == 0) {
                result.setBlock(0, 0, 0, 7, 0);
            }
        } else {
            result.setCuboid(bedrock1, bedrock2, PlotBlock.get(7, 0));
            result.setCuboid(dirt1, dirt2, PlotBlock.get(3, 0));
            result.setCuboid(grass1, grass2, PlotBlock.get(2, 0));
        }
        result.fillBiome("PLAINS");
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return ((SinglePlotAreaManager) PS.get().getPlotAreaManager()).getArea();
    }

    @Override public PlotManager getNewPlotManager() {
        return new SinglePlotManager();
    }

    @Override public void initialize(PlotArea area) {

    }
}
