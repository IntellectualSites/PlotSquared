package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class DenyExitFlag extends BooleanFlag<DenyExitFlag> {

    public static final DenyExitFlag DENY_EXIT_FLAG_TRUE = new DenyExitFlag(true);
    public static final DenyExitFlag DENY_EXIT_FLAG_FALSE = new DenyExitFlag(false);

    protected DenyExitFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_DENY_EXIT);
    }

    @Override protected DenyExitFlag flagOf(@NotNull Boolean value) {
        return value ? DENY_EXIT_FLAG_TRUE : DENY_EXIT_FLAG_FALSE;
    }

}
