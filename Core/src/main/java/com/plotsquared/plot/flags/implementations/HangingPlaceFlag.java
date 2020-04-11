package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
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
