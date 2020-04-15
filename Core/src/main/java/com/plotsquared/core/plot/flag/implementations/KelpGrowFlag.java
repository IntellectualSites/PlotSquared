package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
