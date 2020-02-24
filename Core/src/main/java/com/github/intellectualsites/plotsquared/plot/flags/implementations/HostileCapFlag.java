package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
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
