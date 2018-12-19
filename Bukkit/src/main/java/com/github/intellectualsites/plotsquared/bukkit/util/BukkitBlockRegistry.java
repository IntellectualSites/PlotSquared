package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.object.BlockRegistry;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import lombok.NonNull;
import org.bukkit.Material;

public class BukkitBlockRegistry extends BlockRegistry<Material> {

    public BukkitBlockRegistry(final Material... preInitializedItems) {
        super(Material.class, preInitializedItems);
    }

    @Override public PlotBlock getPlotBlock(@NonNull final Material item) {
        return PlotBlock.get(item.name());
    }

}
