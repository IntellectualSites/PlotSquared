package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class BlockBurnFlag extends BooleanFlag<BlockBurnFlag> {

    public static final BlockBurnFlag BLOCK_BURN_TRUE = new BlockBurnFlag(true);
    public static final BlockBurnFlag BLOCK_BURN_FALSE = new BlockBurnFlag(false);

    private BlockBurnFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_BLOCK_BURN);
    }

    @Override protected BlockBurnFlag flagOf(@NotNull Boolean value) {
        return value ? BLOCK_BURN_TRUE : BLOCK_BURN_FALSE;
    }

}
