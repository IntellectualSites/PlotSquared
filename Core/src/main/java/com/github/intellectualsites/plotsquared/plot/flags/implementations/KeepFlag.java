package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.MathMan;
import org.jetbrains.annotations.NotNull;

public class KeepFlag extends PlotFlag<Object, KeepFlag> {

    public static final KeepFlag KEEP_FLAG_FALSE = new KeepFlag(false);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected KeepFlag(@NotNull Object value) {
        super(value, Captions.FLAG_CATEGORY_MIXED, Captions.FLAG_DESCRIPTION_KEEP);
    }

    @Override public KeepFlag parse(@NotNull String input) throws FlagParseException {
        if (MathMan.isInteger(input)) {
            final long value = Long.parseLong(input);
            if (value < 0) {
                throw new FlagParseException(this, input, Captions.FLAG_ERROR_KEEP);
            } else {
                return flagOf(value);
            }
        }
        switch (input.toLowerCase()) {
            case "true":
                return flagOf(true);
            case "false":
                return flagOf(false);
            default:
                return flagOf(MainUtil.timeToSec(input) * 1000 + System.currentTimeMillis());
        }
    }

    @Override public KeepFlag merge(@NotNull Object newValue) {
        if (newValue.equals(true)) {
            return flagOf(true);
        } else if (newValue.equals(false)) {
            if (getValue().equals(true) || getValue().equals(false)) {
                return this;
            } else {
                return flagOf(newValue);
            }
        } else {
            if (getValue().equals(true)) {
                return this;
            } else if (getValue().equals(false)) {
                return flagOf(newValue);
            } else {
                long currentValue = (long) getValue();
                return flagOf((long) newValue + currentValue);
            }
        }
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "3w 4d 2h";
    }

    @Override protected KeepFlag flagOf(@NotNull Object value) {
        return new KeepFlag(value);
    }

}
