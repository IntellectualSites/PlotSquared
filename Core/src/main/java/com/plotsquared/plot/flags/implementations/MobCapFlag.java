package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
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
