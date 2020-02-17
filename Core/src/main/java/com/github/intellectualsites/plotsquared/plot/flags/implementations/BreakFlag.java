package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BlockTypeListFlag;
import com.sk89q.worldedit.world.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BreakFlag extends BlockTypeListFlag<BreakFlag> {
    public static final BreakFlag BREAK_NONE = new BreakFlag(Collections.emptyList());

    protected BreakFlag(List<BlockType> blockTypeList) {
        super(blockTypeList, Captions.FLAG_DESCRIPTION_BREAK);
    }

    @Override protected BreakFlag flagOf(@NotNull List<BlockType> value) {
        return new BreakFlag(value);
    }
}
