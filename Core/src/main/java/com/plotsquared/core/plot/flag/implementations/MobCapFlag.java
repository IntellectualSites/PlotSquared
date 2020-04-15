package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class MobCapFlag extends NonNegativeIntegerFlag<MobCapFlag> {
    public static final MobCapFlag MOB_CAP_UNLIMITED = new MobCapFlag(Integer.MAX_VALUE);

    protected MobCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_MOB_CAP);
    }

    @Override protected MobCapFlag flagOf(@NotNull Integer value) {
        return new MobCapFlag(value);
    }
}
