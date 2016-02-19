package com.plotsquared.sponge;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongeHybridUtils extends HybridUtils {
    
    @Override
    public int checkModified(final String worldname, final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks) {
        PS.debug("checkModified is not implemented");
        final World world = SpongeUtil.getWorld(worldname);
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    final BlockState state = world.getBlock(x, y, z);
                    final PlotBlock block = SpongeUtil.getPlotBlock(state);
                    boolean same = false;
                    for (final PlotBlock p : blocks) {
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
    public int get_ey(final String worldname, final int sx, final int ex, final int sz, final int ez, final int sy) {
        final World world = SpongeUtil.getWorld(worldname);
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y < 256; y++) {
                    if (y > ey) {
                        final BlockState state = world.getBlock(x, y, z);
                        if ((state != null) && (state.getType() != BlockTypes.AIR)) {
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
        // TODO Auto-generated method stub
        whenDone.run();
    }
    
}
