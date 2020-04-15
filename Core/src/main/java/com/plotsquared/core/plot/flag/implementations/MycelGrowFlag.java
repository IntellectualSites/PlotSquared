package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MycelGrowFlag extends BooleanFlag<MycelGrowFlag> {

    public static final MycelGrowFlag MYCEL_GROW_TRUE = new MycelGrowFlag(true);
    public static final MycelGrowFlag MYCEL_GROW_FALSE = new MycelGrowFlag(false);

    private MycelGrowFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MYCEL_GROW);
    }

    @Override protected MycelGrowFlag flagOf(@NotNull Boolean value) {
        return value ? MYCEL_GROW_TRUE : MYCEL_GROW_FALSE;
    }

}
