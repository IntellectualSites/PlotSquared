package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BlockTypeListFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PlaceFlag extends BlockTypeListFlag<PlaceFlag> {

    public static final PlaceFlag PLACE_NONE = new PlaceFlag(Collections.emptyList());

    protected PlaceFlag(List<BlockTypeWrapper> blockTypeList) {
        super(blockTypeList, Captions.FLAG_DESCRIPTION_PLACE);
    }

    @Override protected PlaceFlag flagOf(@NotNull List<BlockTypeWrapper> value) {
        return new PlaceFlag(value);
    }

}
