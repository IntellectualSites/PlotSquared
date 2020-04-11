package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class SnowFormFlag extends BooleanFlag<SnowFormFlag> {

    public static final SnowFormFlag SNOW_FORM_TRUE = new SnowFormFlag(true);
    public static final SnowFormFlag SNOW_FORM_FALSE = new SnowFormFlag(false);

    private SnowFormFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_SNOW_FORM);
    }

    @Override protected SnowFormFlag flagOf(@NotNull Boolean value) {
        return value ? SNOW_FORM_TRUE : SNOW_FORM_FALSE;
    }

}
