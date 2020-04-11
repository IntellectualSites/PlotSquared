package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class SnowMeltFlag extends BooleanFlag<SnowMeltFlag> {

    public static final SnowMeltFlag SNOW_MELT_TRUE = new SnowMeltFlag(true);
    public static final SnowMeltFlag SNOW_MELT_FALSE = new SnowMeltFlag(false);

    private SnowMeltFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_SNOW_MELT);
    }

    @Override protected SnowMeltFlag flagOf(@NotNull Boolean value) {
        return value ? SNOW_MELT_TRUE : SNOW_MELT_FALSE;
    }

}
