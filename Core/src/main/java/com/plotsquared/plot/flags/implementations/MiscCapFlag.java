package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class MiscCapFlag extends NonNegativeIntegerFlag<MiscCapFlag> {
    public static final MiscCapFlag MISC_CAP_UNLIMITED = new MiscCapFlag(Integer.MAX_VALUE);

    protected MiscCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_MISC_CAP);
    }

    @Override protected MiscCapFlag flagOf(@NotNull Integer value) {
        return new MiscCapFlag(value);
    }
}
