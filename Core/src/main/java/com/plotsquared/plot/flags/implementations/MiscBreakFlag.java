package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MiscBreakFlag extends BooleanFlag<MiscBreakFlag> {

    public static final MiscBreakFlag MISC_BREAK_TRUE = new MiscBreakFlag(true);
    public static final MiscBreakFlag MISC_BREAK_FALSE = new MiscBreakFlag(false);

    private MiscBreakFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MISC_BREAK);
    }

    @Override protected MiscBreakFlag flagOf(@NotNull Boolean value) {
        return value ? MISC_BREAK_TRUE : MISC_BREAK_FALSE;
    }

}
