package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class HangingBreakFlag extends BooleanFlag<HangingBreakFlag> {

    public static final HangingBreakFlag HANGING_BREAK_TRUE = new HangingBreakFlag(true);
    public static final HangingBreakFlag HANGING_BREAK_FALSE = new HangingBreakFlag(false);

    private HangingBreakFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_HANGING_BREAK);
    }

    @Override protected HangingBreakFlag flagOf(@NotNull Boolean value) {
        return value ? HANGING_BREAK_TRUE : HANGING_BREAK_FALSE;
    }

}
