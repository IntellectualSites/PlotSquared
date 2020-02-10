package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public class IntegerFlag extends PlotFlag<Integer> {

    protected IntegerFlag(final int defaultValue, @NotNull Captions flagDescription) {
        super(defaultValue, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
    }

    protected IntegerFlag(@NotNull Captions flagDescription) {
        this(0, flagDescription);
    }

    @Override public Integer parse(@NotNull String input) throws FlagParseException {
        try {
            return Integer.parseInt(input);
        } catch (final Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_INTEGER);
        }
    }

    @Override public Integer merge(@NotNull Integer oldValue, @NotNull Integer newValue) {
        return oldValue + newValue;
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public String getExample() {
        return "10";
    }

}
