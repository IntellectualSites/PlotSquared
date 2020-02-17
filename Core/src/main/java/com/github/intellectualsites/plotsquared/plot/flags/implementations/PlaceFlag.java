package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BlockTypeListFlag;
import com.sk89q.worldedit.world.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PlaceFlag extends BlockTypeListFlag<PlaceFlag> {
    public static final PlaceFlag PLACE_NONE = new PlaceFlag(Collections.emptyList());

    protected PlaceFlag(List<BlockType> blockTypeList) {
        super(blockTypeList, Captions.FLAG_DESCRIPTION_PLACE);
    }

    @Override protected PlaceFlag flagOf(@NotNull List<BlockType> value) {
        return new PlaceFlag(value);
    }
}
