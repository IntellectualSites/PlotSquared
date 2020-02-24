package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import org.jetbrains.annotations.NotNull;

public abstract class IntegerFlag<F extends NumberFlag<Integer, F>> extends NumberFlag<Integer, F> {

    protected IntegerFlag(final int value, int minimum, int maximum,
        @NotNull Caption flagDescription) {
        super(value, minimum, maximum, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
    }

    protected IntegerFlag(@NotNull Caption flagDescription) {
        this(0, Integer.MIN_VALUE, Integer.MAX_VALUE, flagDescription);
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

    @NotNull @Override protected Integer parseNumber(String input) throws FlagParseException {
        try {
            return Integer.parseInt(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_INTEGER);
        }
    }
}
