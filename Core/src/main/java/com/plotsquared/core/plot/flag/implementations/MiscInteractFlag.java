package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MiscInteractFlag extends BooleanFlag<MiscInteractFlag> {

    public static final MiscInteractFlag MISC_INTERACT_TRUE = new MiscInteractFlag(true);
    public static final MiscInteractFlag MISC_INTERACT_FALSE = new MiscInteractFlag(false);

    private MiscInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MISC_INTERACT);
    }

    @Override protected MiscInteractFlag flagOf(@NotNull Boolean value) {
        return value ? MISC_INTERACT_TRUE : MISC_INTERACT_FALSE;
    }

}
