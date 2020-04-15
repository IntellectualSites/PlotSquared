package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class VineGrowFlag extends BooleanFlag<VineGrowFlag> {

    public static final VineGrowFlag VINE_GROW_TRUE = new VineGrowFlag(true);
    public static final VineGrowFlag VINE_GROW_FALSE = new VineGrowFlag(false);

    private VineGrowFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_VINE_GROW);
    }

    @Override protected VineGrowFlag flagOf(@NotNull Boolean value) {
        return value ? VINE_GROW_TRUE : VINE_GROW_FALSE;
    }

}
