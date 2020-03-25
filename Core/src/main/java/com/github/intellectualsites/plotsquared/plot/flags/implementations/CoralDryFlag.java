package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
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