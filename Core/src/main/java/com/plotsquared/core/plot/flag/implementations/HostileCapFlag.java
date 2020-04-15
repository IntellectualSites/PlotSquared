package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class HostileCapFlag extends NonNegativeIntegerFlag<HostileCapFlag> {
    public static final HostileCapFlag HOSTILE_CAP_UNLIMITED =
        new HostileCapFlag(Integer.MAX_VALUE);

    protected HostileCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_HOSTILE_CAP);
    }

    @Override protected HostileCapFlag flagOf(@NotNull Integer value) {
        return new HostileCapFlag(value);
    }
}
