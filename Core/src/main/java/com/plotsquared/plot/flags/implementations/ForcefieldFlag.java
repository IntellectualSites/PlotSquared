package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class ForcefieldFlag extends BooleanFlag<ForcefieldFlag> {

    public static final ForcefieldFlag FORCEFIELD_TRUE = new ForcefieldFlag(true);
    public static final ForcefieldFlag FORCEFIELD_FALSE = new ForcefieldFlag(false);

    private ForcefieldFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_FORCEFIELD);
    }

    @Override protected ForcefieldFlag flagOf(@NotNull Boolean value) {
        return value ? FORCEFIELD_TRUE : FORCEFIELD_FALSE;
    }

}
