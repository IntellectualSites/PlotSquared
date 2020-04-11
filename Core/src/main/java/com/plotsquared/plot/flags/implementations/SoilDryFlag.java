package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class SoilDryFlag extends BooleanFlag<SoilDryFlag> {

    public static final SoilDryFlag SOIL_DRY_TRUE = new SoilDryFlag(true);
    public static final SoilDryFlag SOIL_DRY_FALSE = new SoilDryFlag(false);

    private SoilDryFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_SOIL_DRY);
    }

    @Override protected SoilDryFlag flagOf(@NotNull Boolean value) {
        return value ? SOIL_DRY_TRUE : SOIL_DRY_FALSE;
    }

}
