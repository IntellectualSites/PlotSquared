package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
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
