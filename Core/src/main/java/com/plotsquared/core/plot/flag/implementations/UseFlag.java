package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BlockTypeListFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class UseFlag extends BlockTypeListFlag<UseFlag> {

    public static final UseFlag USE_NONE = new UseFlag(Collections.emptyList());

    protected UseFlag(List<BlockTypeWrapper> blockTypeList) {
        super(blockTypeList, Captions.FLAG_DESCRIPTION_USE);
    }

    @Override protected UseFlag flagOf(@NotNull List<BlockTypeWrapper> value) {
        return new UseFlag(value);
    }

}
