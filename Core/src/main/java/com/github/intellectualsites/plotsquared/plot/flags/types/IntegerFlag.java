package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class IntegerFlag<F extends PlotFlag<Integer, F>> extends PlotFlag<Integer, F> {

    protected IntegerFlag(final int value, @NotNull Caption flagDescription) {
        super(value, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
    }

    protected IntegerFlag(@NotNull Caption flagDescription) {
        this(0, flagDescription);
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        try {
            return flagOf(Integer.parseInt(input));
        } catch (final Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_INTEGER);
        }
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
