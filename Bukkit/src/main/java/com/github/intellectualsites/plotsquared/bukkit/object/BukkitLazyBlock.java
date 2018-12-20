package com.github.intellectualsites.plotsquared.bukkit.object;

import com.github.intellectualsites.plotsquared.plot.object.LazyBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.StringPlotBlock;
import org.bukkit.block.Block;

public class BukkitLazyBlock extends LazyBlock {

    private StringPlotBlock pb;

    public BukkitLazyBlock(Block block) {
        this.pb = (StringPlotBlock) PlotBlock.get(block.getType().toString());
    }

    public BukkitLazyBlock(StringPlotBlock pb) {
        this.pb = pb;
    }

    public StringPlotBlock getPlotBlock() {
        return this.pb;
    }


}
