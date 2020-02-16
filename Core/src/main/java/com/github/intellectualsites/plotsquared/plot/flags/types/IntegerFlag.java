package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class IntegerFlag<F extends PlotFlag<Integer, F>> extends PlotFlag<Integer, F> {
    private final int minimum;
    private final int maximum;

    protected IntegerFlag(final int value, int minimum, int maximum, @NotNull Caption flagDescription) {
        super(value, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
        if (maximum < minimum) {
            throw new IllegalArgumentException("Maximum may not be less than minimum:" + maximum + " < " + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    protected IntegerFlag(@NotNull Caption flagDescription) {
        this(0, Integer.MIN_VALUE, Integer.MAX_VALUE, flagDescription);
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        int parsed;
        try {
            parsed = Integer.parseInt(input);
        } catch (final Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_INTEGER);
        }
        if (parsed < minimum || parsed > maximum) {
            throw new FlagParseException(this, input, Captions.NOT_VALID_NUMBER); // TODO format Caption, provide valid range
        }
        return flagOf(parsed);

    }

    @Override public F merge(@NotNull Integer newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public String getExample() {
        return "10";
    }
}
