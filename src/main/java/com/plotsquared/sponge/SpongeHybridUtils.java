package com.plotsquared.sponge;

import org.bukkit.block.Block;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongeHybridUtils extends HybridUtils {
    
    @Override
    public void analyzePlot(Plot plot, RunnableVal<PlotAnalysis> whenDone) {
        // TODO Auto-generated method stub
        PS.debug("analyzePlot is not implemented");
        if (whenDone != null) {
            whenDone.run();
        }
    }
    
    @Override
    public int checkModified(String worldname, int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks) {
        PS.debug("checkModified is not implemented");
        final World world = SpongeUtil.getWorld(worldname);
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    BlockState state = world.getBlock(x, y, z);
                    PlotBlock block = SpongeMain.THIS.getPlotBlock(state);
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
    public int get_ey(String worldname, int sx, int ex, int sz, int ez, int sy) {
        final World world = SpongeUtil.getWorld(worldname);
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y < 256; y++) {
                    if (y > ey) {
                        BlockState state = world.getBlock(x, y, z);
                        if (state != null && state.getType() != BlockTypes.AIR) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }
    
}
