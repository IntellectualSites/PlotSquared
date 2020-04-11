package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class KelpGrowFlag extends BooleanFlag<KelpGrowFlag> {

    public static final KelpGrowFlag KELP_GROW_TRUE = new KelpGrowFlag(true);
    public static final KelpGrowFlag KELP_GROW_FALSE = new KelpGrowFlag(false);

    private KelpGrowFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_KELP_GROW);
    }

    @Override protected KelpGrowFlag flagOf(@NotNull Boolean value) {
        return value ? KELP_GROW_TRUE : KELP_GROW_FALSE;
    }

}
