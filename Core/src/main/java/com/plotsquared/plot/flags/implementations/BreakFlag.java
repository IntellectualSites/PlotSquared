package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BlockTypeListFlag;
import com.plotsquared.plot.flags.types.BlockTypeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BreakFlag extends BlockTypeListFlag<BreakFlag> {

    public static final BreakFlag BREAK_NONE = new BreakFlag(Collections.emptyList());

    protected BreakFlag(List<BlockTypeWrapper> blockTypeList) {
        super(blockTypeList, Captions.FLAG_DESCRIPTION_BREAK);
    }

    @Override protected BreakFlag flagOf(@NotNull List<BlockTypeWrapper> value) {
        return new BreakFlag(value);
    }

}
