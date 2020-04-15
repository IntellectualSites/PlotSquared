package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class HangingPlaceFlag extends BooleanFlag<HangingPlaceFlag> {

    public static final HangingPlaceFlag HANGING_PLACE_TRUE = new HangingPlaceFlag(true);
    public static final HangingPlaceFlag HANGING_PLACE_FALSE = new HangingPlaceFlag(false);

    private HangingPlaceFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_HANGING_PLACE);
    }

    @Override protected HangingPlaceFlag flagOf(@NotNull Boolean value) {
        return value ? HANGING_PLACE_TRUE : HANGING_PLACE_FALSE;
    }

}
