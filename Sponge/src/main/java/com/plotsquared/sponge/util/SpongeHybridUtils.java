package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;

public class SpongeHybridUtils extends HybridUtils {
    
    @Override
    public int checkModified(String worldName, int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks) {
        World world = SpongeUtil.getWorld(worldName);
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    BlockState state = world.getBlock(x, y, z);
                    PlotBlock block = SpongeUtil.getPlotBlock(state);
                    boolean same = false;
                    for (PlotBlock p : blocks) {
                        if (block.id == p.id) {
                            same = true;
                            break;
                        }
                    }
                    if (!same) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    @Override
    public int get_ey(String worldName, int sx, int ex, int sz, int ez, int sy) {
        World world = SpongeUtil.getWorld(worldName);
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y < 256; y++) {
                    if (y > ey) {
                        BlockState state = world.getBlock(x, y, z);
                        if (state.getType() != BlockTypes.AIR) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }
    
    @Override
    public void analyzeRegion(String world, RegionWrapper region, RunnableVal<PlotAnalysis> whenDone) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
}
