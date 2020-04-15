package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.config.Caption;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class NumberFlag<N extends Number & Comparable<N>, F extends PlotFlag<N, F>>
    extends PlotFlag<N, F> {
    protected final N minimum;
    protected final N maximum;

    protected NumberFlag(@NotNull N value, N minimum, N maximum, @NotNull Caption flagCategory,
        @NotNull Caption flagDescription) {
        super(value, flagCategory, flagDescription);
        if (maximum.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                "Maximum may not be less than minimum:" + maximum + " < " + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        final N parsed = parseNumber(input);
        if (parsed.compareTo(minimum) < 0 || parsed.compareTo(maximum) > 0) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_IN_RANGE, minimum,
                maximum);
        }
        return flagOf(parsed);

    }

    /**
     * Parse the raw string input to the number type.
     * Throw a {@link FlagParseException} if the number couldn't be parsed.
     *
     * @param input the string to parse the number from.
     * @return the parsed number.
     */
    @NotNull protected abstract N parseNumber(String input) throws FlagParseException;
}
