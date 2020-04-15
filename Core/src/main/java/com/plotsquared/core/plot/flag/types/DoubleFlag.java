package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.config.Caption;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import org.jetbrains.annotations.NotNull;

public abstract class DoubleFlag<F extends NumberFlag<Double, F>> extends NumberFlag<Double, F> {

    protected DoubleFlag(@NotNull Double value, Double minimum, Double maximum,
        @NotNull Caption flagDescription) {
        super(value, minimum, maximum, Captions.FLAG_CATEGORY_DOUBLES, flagDescription);
    }

    protected DoubleFlag(@NotNull Double value, @NotNull Caption flagDescription) {
        this(value, Double.MIN_VALUE, Double.MAX_VALUE, flagDescription);
    }

    @Override public F merge(@NotNull Double newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override public String getExample() {
        return "12.175";
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @NotNull @Override protected Double parseNumber(String input) throws FlagParseException {
        try {
            return Double.parseDouble(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_DOUBLE);
        }
    }
}
