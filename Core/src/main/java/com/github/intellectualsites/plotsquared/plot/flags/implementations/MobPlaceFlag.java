package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MobPlaceFlag extends BooleanFlag<MobPlaceFlag> {

    public static final MobPlaceFlag MOB_PLACE_TRUE = new MobPlaceFlag(true);
    public static final MobPlaceFlag MOB_PLACE_FALSE = new MobPlaceFlag(false);

    private MobPlaceFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MOB_PLACE);
    }

    @Override protected MobPlaceFlag flagOf(@NotNull Boolean value) {
        return value ? MOB_PLACE_TRUE : MOB_PLACE_FALSE;
    }

}
