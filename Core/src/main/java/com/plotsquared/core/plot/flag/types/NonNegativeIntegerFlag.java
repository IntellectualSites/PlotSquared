package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.config.Caption;
import org.jetbrains.annotations.NotNull;

public abstract class NonNegativeIntegerFlag<F extends IntegerFlag<F>> extends IntegerFlag<F> {

    protected NonNegativeIntegerFlag(int value, @NotNull Caption flagDescription) {
        super(value, 0, Integer.MAX_VALUE, flagDescription);
    }

    public NonNegativeIntegerFlag(int value, int maximum, @NotNull Caption flagDescription) {
        super(value, 0, maximum, flagDescription);
    }
}
