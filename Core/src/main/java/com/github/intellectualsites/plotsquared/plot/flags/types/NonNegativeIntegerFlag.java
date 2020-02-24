package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import org.jetbrains.annotations.NotNull;

public abstract class NonNegativeIntegerFlag<F extends IntegerFlag<F>> extends IntegerFlag<F> {

    protected NonNegativeIntegerFlag(int value, @NotNull Caption flagDescription) {
        super(value, 0, Integer.MAX_VALUE, flagDescription);
    }

    public NonNegativeIntegerFlag(int value, int maximum, @NotNull Caption flagDescription) {
        super(value, 0, maximum, flagDescription);
    }
}
