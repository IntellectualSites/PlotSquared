package com.plotsquared.plot.flags.types;

import com.plotsquared.config.Caption;
import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import org.jetbrains.annotations.NotNull;

public abstract class LongFlag<F extends NumberFlag<Long, F>> extends NumberFlag<Long, F> {

    protected LongFlag(@NotNull Long value, Long minimum, Long maximum,
        @NotNull Caption flagDescription) {
        super(value, minimum, maximum, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
    }

    protected LongFlag(@NotNull Long value, @NotNull Caption flagDescription) {
        this(value, Long.MIN_VALUE, Long.MAX_VALUE, flagDescription);
    }

    @Override public F merge(@NotNull Long newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "123456789";
    }

    @NotNull @Override protected Long parseNumber(String input) throws FlagParseException {
        try {
            return Long.parseLong(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.NOT_A_NUMBER, input);
        }
    }
}
