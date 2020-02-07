package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public abstract class BooleanFlag extends PlotFlag<Boolean> {

    private static final Collection<String> positiveValues = Arrays.asList("1", "yes", "allow", "true");
    private static final Collection<String> negativeValues = Arrays.asList("0", "no", "deny", "disallow", "false");

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected BooleanFlag(boolean value) {
        super(value);
    }

    /**
     * Construct a new boolean flag, with
     * {@code false} as the default value.
     */
    protected BooleanFlag() {
        this(false);
    }

    @Override public Boolean parse(@NotNull String input) throws FlagParseException {
        if (positiveValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.setFlagValue(true);
        } else if (negativeValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.setFlagValue(false);
        } else {
            throw new FlagParseException(this, input);
        }
    }

    @Override public Boolean merge(@NotNull Boolean oldValue, @NotNull Boolean newValue) {
        return this.setFlagValue(oldValue || newValue);
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

}
