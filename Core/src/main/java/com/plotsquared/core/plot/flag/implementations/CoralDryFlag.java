package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class CoralDryFlag extends BooleanFlag<CoralDryFlag> {

    public static final CoralDryFlag CORAL_DRY_TRUE = new CoralDryFlag(true);
    public static final CoralDryFlag CORAL_DRY_FALSE = new CoralDryFlag(false);

    private CoralDryFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_CORAL_DRY);
    }

    @Override protected CoralDryFlag flagOf(@NotNull Boolean value) {
        return value ? CORAL_DRY_TRUE : CORAL_DRY_FALSE;
    }

}
