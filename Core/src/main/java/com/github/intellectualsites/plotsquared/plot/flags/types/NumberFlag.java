package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class NumberFlag<N extends Number & Comparable<N>, F extends PlotFlag<N, F>> extends PlotFlag<N, F> {
    protected final N minimum;
    protected final N maximum;

    protected NumberFlag(@NotNull N value, N minimum, N maximum, @NotNull Caption flagCategory,
        @NotNull Caption flagDescription) {
        super(value, flagCategory, flagDescription);
        if (maximum.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("Maximum may not be less than minimum:" + maximum + " < " + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        final N parsed = parseNumber(input);
        if (parsed.compareTo(minimum) < 0 || parsed.compareTo(maximum) > 0) {
            throw new FlagParseException(this, input,
                Captions.NOT_VALID_NUMBER); // TODO format Caption, provide valid range
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
