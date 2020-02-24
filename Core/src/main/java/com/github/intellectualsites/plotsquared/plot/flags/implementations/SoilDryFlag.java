package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
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
