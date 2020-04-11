package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MiscPlaceFlag extends BooleanFlag<MiscPlaceFlag> {

    public static final MiscPlaceFlag MISC_PLACE_TRUE = new MiscPlaceFlag(true);
    public static final MiscPlaceFlag MISC_PLACE_FALSE = new MiscPlaceFlag(false);

    private MiscPlaceFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MISC_PLACE);
    }

    @Override protected MiscPlaceFlag flagOf(@NotNull Boolean value) {
        return value ? MISC_PLACE_TRUE : MISC_PLACE_FALSE;
    }

}
