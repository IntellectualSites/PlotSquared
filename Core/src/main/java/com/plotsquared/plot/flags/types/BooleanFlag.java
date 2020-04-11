package com.plotsquared.plot.flags.types;

import com.plotsquared.config.Caption;
import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public abstract class BooleanFlag<F extends PlotFlag<Boolean, F>> extends PlotFlag<Boolean, F> {

    private static final Collection<String> positiveValues =
        Arrays.asList("1", "yes", "allow", "true");
    private static final Collection<String> negativeValues =
        Arrays.asList("0", "no", "deny", "disallow", "false");

    /**
     * Construct a new flag instance.
     *
     * @param value       Flag value
     * @param description Flag description
     */
    protected BooleanFlag(final boolean value, final Caption description) {
        super(value, Captions.FLAG_CATEGORY_BOOLEAN, description);
    }

    /**
     * Construct a new boolean flag, with
     * {@code false} as the default value.
     *
     * @param description Flag description
     */
    protected BooleanFlag(final Captions description) {
        this(false, description);
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        if (positiveValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(true);
        } else if (negativeValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(false);
        } else {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_BOOLEAN);
        }
    }

    @Override public F merge(@NotNull Boolean newValue) {
        return this.flagOf(getValue() || newValue);
    }

    @Override public String getExample() {
        return "true";
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false");
    }
}
